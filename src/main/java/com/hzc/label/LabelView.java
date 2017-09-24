package com.hzc.label;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jonch on 2017/9/21.
 */

public class LabelView extends LinearLayout {

    //数据适配器
    private TagAdapter adapter;

    //是否需要重绘
    private boolean redraw = true;

    //是否启用移除功能
    private boolean enableDelete = true;

    private int themeNormal = R.style.tagItemNormal;

    private int themeSelected = R.style.tagItemSelected;

    /**
     * 自定义主题样式
     *
     * @param themeNormal
     */
    public void setThemeNormal(int themeNormal) {
        this.themeNormal = themeNormal;
    }

    /**
     * 自定义主题样式
     *
     * @param themeSelected
     */
    public void setThemeSelected(int themeSelected) {
        this.themeSelected = themeSelected;
    }

    /**
     * 全局启用删除功能
     *
     * @param bool
     */
    public void setEnableDelete(boolean bool) {
        enableDelete = bool;
    }

    //所有的textview控件
    List<TextView> tvlist = new ArrayList<>();

    private void info(String message) {
        System.out.println(message);
    }

    private void reDrawList() {
        redraw = true;
        this.postInvalidate();
    }


    /**
     * 绘制
     */
    private void drawList() {
        if (!redraw)
            return;
        int total = 0;
        tvlist.clear();
        Map<Integer, List<View>> result = new LinkedHashMap<>();
        int i = 0;
        for (Object obj : this.adapter.list) {
            //初始化数据层
            if (result.get(i) == null) {
                List<View> list = new ArrayList<>();
                result.put(i, list);
            }
            List<View> views = result.get(i);

            View item = LayoutInflater.from(this.getContext()).inflate(R.layout.tag_item, null);
            TextView text = (TextView) item.findViewById(R.id.txt_jc_text);
            text.setText(adapter.getText(obj));

            if (!adapter.enableDelete(obj)) {
                item.findViewById(R.id.img_jc_clean).setVisibility(GONE);
            }

            //当前测量出来的控件的长度
            int currentWidth = total;

            View v = text;
            while (!(v instanceof FrameLayout)) {
                total += v.getPaddingLeft();
                total += v.getPaddingRight();
                v = (View) v.getParent();
            }
            total += v.getPaddingRight();
            total += v.getPaddingLeft();
            //在父级节点存放T
            v.setTag(obj);
            //测绘文字的长度
            Paint paint = new Paint();
            paint.setTextSize(text.getTextSize());
            int oneWidth = (int) paint.measureText(text.getText().toString());
            total += oneWidth;

            if (total > getWidth()) {//当前的item总和要比总的宽度要长，保留到下一次加入队伍
                i++;
                List<View> list = new ArrayList<>();
                list.add(item);
                result.put(i, list);
                total = total - currentWidth;
            } else {//依然有空隙，可以继续加入当前行
                views.add(item);
            }


            tvlist.add(text);
        }

        //基本行
        LinearLayout base = new LinearLayout(this.getContext());
        LayoutParams baselp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        base.setLayoutParams(baselp);
        base.setOrientation(LinearLayout.VERTICAL);

        //每一个KEY就是一个tag行，每一个tag行包含多个tag
        for (Integer key : result.keySet()) {
            List<View> views = result.get(key);
            LinearLayout root = new LinearLayout(this.getContext());
            LayoutParams rootlp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            root.setLayoutParams(rootlp);
            root.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < views.size(); j++) {
                View v = views.get(j);
                TextView textView = (TextView) v.findViewById(R.id.txt_jc_text);
                textView.setOnClickListener(new OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View v) {
                        for (TextView tv : tvlist) {
                            tv.setSelected(false);
                            tv.setTextAppearance(R.style.tagItemNormal);
                        }
                        v.setSelected(true);
                        ((TextView) v).setTextAppearance(R.style.tagItemSelected);
                        adapter.onClick(((View) v.getParent()).getTag());
                    }
                });

                ImageView imgClear = (ImageView) v.findViewById(R.id.img_jc_clean);
                imgClear.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Object object = ((View) v.getParent()).getTag();
                        for (int i = 0; i < adapter.list.size(); i++) {
                            if (adapter.list.get(i) == object) {
                                adapter.list.remove(i);
                                removeAllViews();
                                reDrawList();
                                break;
                            }
                        }
                        adapter.onDelete(object);
                    }
                });
                root.addView(v);
            }
            base.addView(root);
        }

        addView(base);

        redraw = false;
    }

    public void setAdapter(TagAdapter tagAdapter) {
        this.removeAllViews();
        this.adapter = tagAdapter;
        this.redraw = true;
    }

    @Override
    public void addView(View child) {
        super.addView(child);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        info("info=onWindowFocusChanged");
        super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawList();
    }


    /**
     * you need new TagView().new TagAdapter(T)...
     * or
     * you can extends TagAdapter
     *
     * @param <T>
     */
    public abstract class TagAdapter<T> {

        List<T> list;

        public TagAdapter(List<T> list) {
            this.list = list;
        }

        public abstract String getText(T t);

        public abstract void onClick(T t);

        public abstract void onDelete(T t);

        public boolean enableDelete(T t) {
            return enableDelete;
        }

    }

    public LabelView(Context context) {
        this(context, null);
        setOrientation(LinearLayout.VERTICAL);
    }

    public LabelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LabelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


}
