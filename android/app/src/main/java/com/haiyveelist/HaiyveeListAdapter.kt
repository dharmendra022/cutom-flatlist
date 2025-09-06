package com.haiyveelist

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactRootView
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext

private const val TAG = "HaiyveeListAdapter"
private const val APP_NAME = "HaiyveeCell"

class HaiyveeListAdapter(
  private val themedCtx: ThemedReactContext,
  private val reactInstanceManager: ReactInstanceManager,
  private val onItemClick: (id: String) -> Unit
) : RecyclerView.Adapter<HaiyveeListAdapter.CellVH>() {

  private val items = mutableListOf<ReadableMap>()

  init {
    setHasStableIds(true) // smoother paints
  }

  fun setItems(arr: ReadableArray?) {
    items.clear()
    if (arr != null) {
      for (i in 0 until arr.size()) {
        arr.getMap(i)?.let { items.add(it) }
      }
    }
    Log.d(TAG, "setItems size=${items.size}")
    notifyDataSetChanged()
  }

  override fun getItemId(position: Int): Long {
    val idStr = items.getOrNull(position)?.getString("id") ?: position.toString()
    return idStr.hashCode().toLong()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellVH {
    val root = ReactRootView(parent.context)

    // CRUCIAL: give the cell real layout params (height = WRAP_CONTENT)
    val lp = RecyclerView.LayoutParams(
      RecyclerView.LayoutParams.MATCH_PARENT,
      RecyclerView.LayoutParams.WRAP_CONTENT
    )
    root.layoutParams = lp

    // Small min height helps first paint while RN measures
    root.minimumHeight = 1

    return CellVH(root)
  }

  override fun onBindViewHolder(holder: CellVH, position: Int) {
    val m = items[position]
    val id = m.getString("id") ?: position.toString()
    val title = m.getString("title") ?: "User"
    val subtitle = m.getString("subtitle") ?: ""
    val image = m.getString("image") ?: ""

    val props = Bundle().apply {
      putString("id", id)
      putString("title", title)
      putString("subtitle", subtitle)
      putString("image", image)
      // You can also pass an index for debug:
      putInt("index", position)
    }

    val ctxReady = reactInstanceManager.currentReactContext != null

    if (!holder.started) {
      if (ctxReady) {
        Log.d(TAG, "startReactApplication for id=$id pos=$position")
        holder.root.startReactApplication(reactInstanceManager, APP_NAME, props)
        holder.started = true
      } else {
        // JS not ready yet — bind later (parent will call notifyDataSetChanged once JS initializes)
        Log.d(TAG, "JS not ready; deferring mount for pos=$position")
        holder.deferProps = props
      }
    } else {
      // Already started: update props (Fabric/Paper compatibility)
      try {
        val method = ReactRootView::class.java.getMethod("setAppProperties", Bundle::class.java)
        method.invoke(holder.root, props)
      } catch (_: NoSuchMethodException) {
        holder.root.appProperties = props
      }
    }

    // Make sure it lays out
    holder.root.post {
      holder.root.requestLayout()
      holder.root.invalidate()
    }

    holder.root.setOnClickListener { onItemClick(id) }
  }

  override fun onViewRecycled(holder: CellVH) {
    super.onViewRecycled(holder)
    // Avoid leaking RN views/instances; safe to unmount here
    try {
      holder.root.unmountReactApplication()
    } catch (t: Throwable) {
      // ignore
    }
    holder.started = false
    holder.deferProps = null
  }

  /** Call this after React context becomes available to mount any deferred cells. */
  fun onReactContextReady() {
    if (reactInstanceManager.currentReactContext != null) {
      Log.d(TAG, "React context ready — refreshing adapter")
      notifyDataSetChanged()
    }
  }

  override fun getItemCount(): Int = items.size

  class CellVH(val root: ReactRootView) : RecyclerView.ViewHolder(root) {
    var started: Boolean = false
    var deferProps: Bundle? = null
  }
}
