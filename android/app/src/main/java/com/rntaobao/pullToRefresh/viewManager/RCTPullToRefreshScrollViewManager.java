package com.rntaobao.pullToRefresh.viewManager;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.rntaobao.pullToRefresh.event.LoadingLayoutScrollPositionChangeEvent;
import com.rntaobao.pullToRefresh.event.OnRefreshStartEvent;
import com.rntaobao.pullToRefresh.event.PullToRefreshStateChangeEvent;
import com.rntaobao.pullToRefresh.view.RCTPullToRefreshScrollView;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * {@link RCTPullToRefreshScrollView}与js层的接口
 */

public class RCTPullToRefreshScrollViewManager extends ViewGroupManager<RCTPullToRefreshScrollView> {

    private ThemedReactContext context;
    private EventDispatcher mEventDispatcher;

    private static final String NAME = "RCTPullToRefreshScrollView";

    private static final String CUSTOM_DIRECT_EVENT_TYPE_CONSTANTS_FIELD_REGISTRATION_NAME = "registrationName";
    private static final String JS_CALLBACK_NAME_PULL_TO_REFRESH_STATE_CHANGE = "onPullToRefreshStateChange";
    private static final String JS_CALLBACK_NAME_LOADING_LAYOUT_SCROLL_POSITION_CHANGE = "onLoadingLayoutScrollPositionChange";
    private static final String JS_CALLBACK_NAME_ON_REFRESH_START = "onRefreshStart";

    private static final int COMMAND_ID_REFRESH_COMPLETE = 1;

    @Override
    protected RCTPullToRefreshScrollView createViewInstance(ThemedReactContext reactContext) {

        context = reactContext;
        mEventDispatcher = context.getNativeModule(UIManagerModule.class).getEventDispatcher();

        return new RCTPullToRefreshScrollView(reactContext);
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * 注意js端传来的prop数值单位是dp，而java层需要的是px
     */
    @ReactProp(name = "minDraggedDistanceToRefresh")
    public void setMinDraggedDistanceToRefresh(RCTPullToRefreshScrollView v, int distanceDp) {
        v.setMinDraggedDistanceToRefreshDp(distanceDp);
    }

    @Override
    protected void addEventEmitters(final ThemedReactContext reactContext, RCTPullToRefreshScrollView view) {

        // TODO 需要创建event池以复用event，节省内存

        /**
         * {@link RCTPullToRefreshScrollView.OnPullToRefreshStateChangeListener}
         * 向js端发送{@link com.rntaobao.pullToRefresh.view.RCTPullToRefreshScrollView.State}变化的信息
         * */
        view.setOnPullToRefreshStateChangeListener(new RCTPullToRefreshScrollView.OnPullToRefreshStateChangeListener() {
            @Override
            public void onPullToRefreshStateChange(RCTPullToRefreshScrollView v, RCTPullToRefreshScrollView.State state) {

                PullToRefreshStateChangeEvent event = new PullToRefreshStateChangeEvent(v.getId(), state);
                mEventDispatcher.dispatchEvent(event);

            }
        });

        /**
         * {@link com.rntaobao.pullToRefresh.view.RCTPullToRefreshScrollView.OnLoadingLayoutScrollPositionChangeListener}
         * 向js端发送其滚动位置变化的信息
         * */
        view.setOnLoadingLayoutScrollPositionChangeListener(new RCTPullToRefreshScrollView.OnLoadingLayoutScrollPositionChangeListener() {
            @Override
            public void onLoadingLayoutScrollPositionChange(RCTPullToRefreshScrollView v, float loadingLayoutScrollPositionRatio) {

                LoadingLayoutScrollPositionChangeEvent event = new LoadingLayoutScrollPositionChangeEvent(v.getId(), loadingLayoutScrollPositionRatio);
                mEventDispatcher.dispatchEvent(event);
            }
        });

        /**
         * {@link com.rntaobao.pullToRefresh.view.RCTPullToRefreshScrollView.OnRefreshListener}
         * 向js端发送刷新开始的消息
         * */
        view.setOnRefreshListener(new RCTPullToRefreshScrollView.OnRefreshListener() {
            @Override
            public void onRefresh(RCTPullToRefreshScrollView v) {
                OnRefreshStartEvent event = new OnRefreshStartEvent(v.getId());
                mEventDispatcher.dispatchEvent(event);
            }
        });
    }

    @Nullable
    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {

        Map pullToRefreshStateChangeEventFieldMap = MapBuilder.of(CUSTOM_DIRECT_EVENT_TYPE_CONSTANTS_FIELD_REGISTRATION_NAME, JS_CALLBACK_NAME_PULL_TO_REFRESH_STATE_CHANGE);
        Map loadingLayoutScrollPositionChangeEventFieldMap = MapBuilder.of(CUSTOM_DIRECT_EVENT_TYPE_CONSTANTS_FIELD_REGISTRATION_NAME, JS_CALLBACK_NAME_LOADING_LAYOUT_SCROLL_POSITION_CHANGE);
        Map onRefreshStartEventFieldMap = MapBuilder.of(CUSTOM_DIRECT_EVENT_TYPE_CONSTANTS_FIELD_REGISTRATION_NAME, JS_CALLBACK_NAME_ON_REFRESH_START);

        Map eventsToFieldsMap = MapBuilder.of(PullToRefreshStateChangeEvent.EVENT_NAME, pullToRefreshStateChangeEventFieldMap, LoadingLayoutScrollPositionChangeEvent.EVENT_NAME, loadingLayoutScrollPositionChangeEventFieldMap, OnRefreshStartEvent.EVENT_NAME, onRefreshStartEventFieldMap);
        return eventsToFieldsMap;
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of("notifyRefreshComplete", COMMAND_ID_REFRESH_COMPLETE);
    }

    @Override
    public void receiveCommand(RCTPullToRefreshScrollView root, int commandId, @Nullable ReadableArray args) {

        switch (commandId) {
            case COMMAND_ID_REFRESH_COMPLETE:
                root.notifyRefreshComplete();
                break;
            default:
                break;
        }

    }

    /**
     * 需要java来负责本层的layout而非js，以便支持{@link com.rntaobao.pullToRefresh.view.RCTPullToRefreshLoadingLayout}效果所必须的负的padding设置
     * 而css不支持负的padding
     */
    @Override
    public boolean needsCustomLayoutForChildren() {
        return true;
    }
}
