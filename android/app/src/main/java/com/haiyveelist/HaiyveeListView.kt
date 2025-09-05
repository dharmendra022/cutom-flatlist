package com.haiyvee

import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.react.ReactApplication
import com.facebook.react.ReactInstanceManager
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter

class HaiyveeListView(private val themedCtx: ThemedReactContext) : FrameLayout(themedCtx) {

  private val reactInstanceManager: ReactInstanceManager by lazy {
    val app = themedCtx.reactApplicationContext.applicationContext as ReactApplication
    app.reactNativeHost.reactInstanceManager
  }

  private val swipe: SwipeRefreshLayout
  private val rv: RecyclerView
  private val adapter: HaiyveeListAdapter

  private var threshold = 0.7f
  private var loadingFooterVisible = false
  private var isRefreshing = false

  init {
    swipe = SwipeRefreshLayout(context)
    rv = RecyclerView(context).apply {
      id = View.generateViewId()
      layoutManager = LinearLayoutManager(context)
      setHasFixedSize(false)
      itemAnimator = null                   // avoid first-paint animations
      setItemViewCacheSize(10)
    }

    adapter = HaiyveeListAdapter(
      themedCtx,
      reactInstanceManager
    ) { id ->
      val map = Arguments.createMap().apply { putString("id", id) }
      sendEvent("onItemPress", map)
    }

    rv.adapter = adapter
    swipe.addView(rv, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    addView(swipe, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

    // 🔧 Wait for React context (JS runtime) if it's not ready yet
    if (reactInstanceManager.currentReactContext == null) {
      // For RN versions where the listener is nested inside ReactInstanceManager:
      val listener = object : ReactInstanceManager.ReactInstanceEventListener {
        override fun onReactContextInitialized(context: ReactContext) {
          // After JS is ready, force list to rebind and paint
          rv.post {
            adapter.notifyDataSetChanged()
            rv.requestLayout()
            rv.invalidate()
          }
          reactInstanceManager.removeReactInstanceEventListener(this)
        }
      }
      reactInstanceManager.addReactInstanceEventListener(listener)

      // If your RN version uses a top-level ReactInstanceEventListener:
      // import com.facebook.react.ReactInstanceEventListener
      // val listener = object : ReactInstanceEventListener { ... }
      // reactInstanceManager.addReactInstanceEventListener(listener)
    }

    swipe.setOnRefreshListener {
      isRefreshing = true
      sendEvent("onRefresh", Arguments.createMap())
    }

    rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy <= 0) return
        val lm = recyclerView.layoutManager as LinearLayoutManager
        val total = adapter.itemCount
        if (total == 0) return
        val lastVisible = lm.findLastVisibleItemPosition()
        val ratio = (lastVisible + 1).toFloat() / total.toFloat()
        if (ratio >= threshold && !loadingFooterVisible && !isRefreshing) {
          loadingFooterVisible = true
          sendEvent("onEndReached", Arguments.createMap())
        }
      }
    })
  }

  private fun sendEvent(eventName: String, payload: com.facebook.react.bridge.WritableMap) {
    themedCtx.getJSModule(RCTEventEmitter::class.java).receiveEvent(this.id, eventName, payload)
  }

  // Props from JS
  fun setData(arr: ReadableArray?) {
    rv.post {
      adapter.setItems(arr)
      loadingFooterVisible = false
      setRefreshing(false)
      rv.requestLayout()
      rv.invalidate()
    }
  }

  fun setRefreshing(refreshing: Boolean) {
    isRefreshing = refreshing
    swipe.isRefreshing = refreshing
  }

  fun setEndReachedThreshold(v: Float) { threshold = v.coerceIn(0f, 1f) }

  fun setContentPadding(top: Int? = null, bottom: Int? = null) {
    val padTop = top ?: rv.paddingTop
    val padBottom = bottom ?: rv.paddingBottom
    rv.setPadding(rv.paddingLeft, padTop, rv.paddingRight, padBottom)
    rv.clipToPadding = false
  }
}
