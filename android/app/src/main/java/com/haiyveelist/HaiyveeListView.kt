package com.haiyveelist

import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.react.ReactApplication
import com.facebook.react.ReactInstanceEventListener
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
  private var hasMore = true

  init {
    // Container: SwipeRefreshLayout -> RecyclerView
    swipe = SwipeRefreshLayout(context).apply {
      layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    rv = RecyclerView(context).apply {
      id = View.generateViewId()
      layoutManager = LinearLayoutManager(context)
      setHasFixedSize(false)
      itemAnimator = null                     // avoid first-paint animations
      setItemViewCacheSize(10)
      layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
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

    // Listen for JS runtime readiness; mount/refresh ReactRootViews afterwards
    if (reactInstanceManager.currentReactContext == null) {
      val listener = object : ReactInstanceEventListener {
        override fun onReactContextInitialized(context: ReactContext) {
          rv.post {
            (rv.adapter as? HaiyveeListAdapter)?.onReactContextReady()
            rv.requestLayout()
            rv.invalidate()
          }
          reactInstanceManager.removeReactInstanceEventListener(this)
        }
      }
      reactInstanceManager.addReactInstanceEventListener(listener)
    }

    // Pull-to-refresh
    swipe.setOnRefreshListener {
      isRefreshing = true
      sendEvent("onRefresh", Arguments.createMap())
    }

    // Infinite scroll
    rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy <= 0) return
        val lm = recyclerView.layoutManager as LinearLayoutManager
        val total = adapter.itemCount
        if (total == 0) return

        val lastVisible = lm.findLastVisibleItemPosition()
        val ratio = (lastVisible + 1).toFloat() / total.toFloat()

        // Fire once per "page" when threshold crossed and we still have more
        if (ratio >= threshold && !loadingFooterVisible && !isRefreshing && hasMore) {
          loadingFooterVisible = true
          sendEvent("onEndReached", Arguments.createMap())
        }
      }
    })
  }

  /** Utility to emit events to JS */
  private fun sendEvent(eventName: String, payload: com.facebook.react.bridge.WritableMap) {
    themedCtx.getJSModule(RCTEventEmitter::class.java).receiveEvent(this.id, eventName, payload)
  }

  // ---------------- Props from JS ----------------

  /** Set/replace list data (ReadableArray of objects) */
  fun setData(arr: ReadableArray?) {
    rv.post {
      val count = arr?.size() ?: 0
      Log.d("HaiyveeListView", "setData size=$count")
      adapter.setItems(arr)           // adapter converts ReadableArray -> List<ReadableMap> & notifies
      loadingFooterVisible = false    // allow next onEndReached
      setRefreshing(false)
      rv.requestLayout()
    }
  }

  /** Control the pull-to-refresh spinner */
  fun setRefreshing(refreshing: Boolean) {
    isRefreshing = refreshing
    swipe.isRefreshing = refreshing
  }

  /** Whether there is more data to paginate */
  fun setHasMore(v: Boolean) {
    hasMore = v
    if (!v) loadingFooterVisible = false
  }

  /** Threshold for onEndReached (0..1) */
  fun setEndReachedThreshold(v: Float) {
    threshold = v.coerceIn(0f, 1f)
  }

  /** Optional: content padding */
  fun setContentPadding(top: Int? = null, bottom: Int? = null) {
    val padTop = top ?: rv.paddingTop
    val padBottom = bottom ?: rv.paddingBottom
    rv.setPadding(rv.paddingLeft, padTop, rv.paddingRight, padBottom)
    rv.clipToPadding = false
  }
}
