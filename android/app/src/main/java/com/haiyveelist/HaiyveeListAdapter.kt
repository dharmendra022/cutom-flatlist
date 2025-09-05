package com.haiyvee

import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactRootView
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext

private const val APP_NAME = "HaiyveeCell" // must match index.js registration

class HaiyveeListAdapter(
  private val themedCtx: ThemedReactContext,
  private val reactInstanceManager: ReactInstanceManager,
  private val onItemClick: (id: String) -> Unit
) : RecyclerView.Adapter<HaiyveeListAdapter.CellVH>() {

  private val items = mutableListOf<ReadableMap>()

  init {
    setHasStableIds(true) // helps RecyclerView paint deterministically
  }

  fun setItems(arr: ReadableArray?) {
    items.clear()
    if (arr != null) {
      for (i in 0 until arr.size()) {
        arr.getMap(i)?.let { items.add(it) }
      }
    }
    notifyDataSetChanged()
  }

  override fun getItemId(position: Int): Long {
    val idStr = items.getOrNull(position)?.getString("id") ?: position.toString()
    return idStr.hashCode().toLong()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellVH {
    // Only create the root. Do NOT call startReactApplication here.
    val root = ReactRootView(parent.context)
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
    }

    // If we haven't started the React application for this root yet, do it now with initial props.
    if (!holder.started) {
      holder.root.startReactApplication(reactInstanceManager, APP_NAME, props)
      holder.started = true
    } else {
      // Already started: update props (Paper or Fabric)
      try {
        val method = ReactRootView::class.java.getMethod("setAppProperties", Bundle::class.java)
        method.invoke(holder.root, props)
      } catch (_: NoSuchMethodException) {
        holder.root.appProperties = props
      }
    }

    // Force a redraw to avoid waiting for user interaction
    holder.root.post {
      holder.root.requestLayout()
      holder.root.invalidate()
    }

    holder.root.setOnClickListener { onItemClick(id) }
  }

  override fun getItemCount(): Int = items.size

  class CellVH(val root: ReactRootView) : RecyclerView.ViewHolder(root) {
    var started: Boolean = false
  }
}
