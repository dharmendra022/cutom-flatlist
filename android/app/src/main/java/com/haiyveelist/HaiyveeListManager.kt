package com.haiyveelist

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class HaiyveeListManager : SimpleViewManager<HaiyveeListView>() {
  override fun getName() = "HaiyveeList"
  override fun createViewInstance(ctx: ThemedReactContext) = HaiyveeListView(ctx)

  @ReactProp(name = "data")
  fun setData(view: HaiyveeListView, arr: ReadableArray?) = view.setData(arr)

  @ReactProp(name = "refreshing")
  fun setRefreshing(view: HaiyveeListView, refreshing: Boolean) = view.setRefreshing(refreshing)

  @ReactProp(name = "onEndReachedThreshold")
  fun setThreshold(view: HaiyveeListView, t: Float) = view.setEndReachedThreshold(t)

  @ReactProp(name = "contentPaddingTop")
  fun setPadTop(view: HaiyveeListView, top: Int) = view.setContentPadding(top = top)

  @ReactProp(name = "contentPaddingBottom")
  fun setPadBottom(view: HaiyveeListView, bottom: Int) = view.setContentPadding(bottom = bottom)

  override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> {
    return MapBuilder.of(
      "onRefresh", MapBuilder.of("registrationName", "onRefresh"),
      "onEndReached", MapBuilder.of("registrationName", "onEndReached"),
      "onItemPress", MapBuilder.of("registrationName", "onItemPress")
    )
  }
}
