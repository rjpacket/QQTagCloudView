package com.rjp.qqtagcloud;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * author : Gimpo create on 2018/12/4 15:42
 * email  : jimbo922@163.com
 */
public class TagCloudView extends View {

    private Paint paint;
    private Paint paint1;
    private int width;
    private int height;
    private List<Tag> showTags = new ArrayList<>();
    private List<String> tags;
    public static final int MIN_TEXT_SIZE = 14;

    public TagCloudView(Context context) {
        this(context, null);
    }

    public TagCloudView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);

        paint1 = new Paint();
        paint1.setAntiAlias(true);
        paint1.setColor(Color.parseColor("#FFC040"));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);

        showTags.clear();
        computeSingleRect(tags, dp2px(30), 0, 0, width, height);
    }

    public static int dp2px(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public void setData(List<String> tags) {
        if (tags == null || tags.size() == 0) {
            throw new IllegalArgumentException("tags can not be null...");
        }
        this.tags = tags;
        invalidate();
    }

    public void computeSingleRect(List<String> tags, int textSize, int pLeft, int pTop, int pRight, int pBottom) {
        if (tags == null || tags.size() == 0 || textSize < MIN_TEXT_SIZE || pBottom == 0 || pRight == 0 || pLeft >= pRight || pTop >= pBottom) {
            return;
        }
        int cLeft = 0;
        int cTop = 0;
        int cRight = 0;
        int cBottom = 0;
        int textWidth = 0;
        int textHeight = 0;
        int size = tags.size();
        int index = (int) (Math.random() * (size - 1));
        String name = tags.get(index);
        //计算当前rect的宽高
        int rectWidth = pRight - pLeft;
        int rectHeight = pBottom - pTop;
        if (rectWidth > rectHeight) {
            //父布局长大于高，横向布局合适
            paint.setTextSize(textSize);
            textWidth = (int) paint.measureText(name);
            textHeight = (int) (paint.getFontMetrics().bottom - paint.getFontMetrics().top);
            if (textHeight > rectHeight) {
                //记录之前的textsize
                int beforeTextSize = textSize;
                while (textHeight > rectHeight) {
                    textSize--;
                    paint.setTextSize(textSize);
                    textHeight = (int) (paint.getFontMetrics().bottom - paint.getFontMetrics().top);
                }
                textWidth = (int) paint.measureText(name);
                while (textWidth > rectWidth) {
                    textSize--;
                    paint.setTextSize(textSize);
                    textWidth = (int) paint.measureText(name);
                }
                if(textSize < MIN_TEXT_SIZE){
                    return;
                }
                textHeight = (int) (paint.getFontMetrics().bottom - paint.getFontMetrics().top);
                cLeft = pLeft;
                cTop = pTop;
                cRight = textWidth + pLeft;
                cBottom = textHeight + pTop;
                showTags.add(new Tag(name, textSize, cLeft, cTop, cRight, cBottom));

                textWidth = (int) paint.measureText(name);
                if (pRight - cRight > textWidth) {
                    //右
                    computeSingleRect(tags, beforeTextSize, cRight, pTop, pRight, pBottom);
                } else {
                    //右
                    computeSingleRect(tags, --textSize, cRight, pTop, pRight, pBottom);
                }
            } else {
                if (textWidth >= rectWidth) {
                    while (textWidth > rectWidth) {
                        textSize--;
                        paint.setTextSize(textSize);
                        textWidth = (int) paint.measureText(name);
                    }
                    if(textSize < MIN_TEXT_SIZE){
                        return;
                    }
                    textHeight = (int) (paint.getFontMetrics().bottom - paint.getFontMetrics().top);
                    cLeft = pLeft;
                    cTop = pTop;
                    cRight = pRight;
                    cBottom = cTop + textHeight;
                    showTags.add(new Tag(name, textSize, cLeft, cTop, cRight, cBottom));

                    //下
                    textSize += 4;
                    computeSingleRect(tags, textSize, cLeft, cBottom, cRight, pBottom);
                } else {
                    cLeft = (int) (Math.random() * (rectWidth / 3)) + pLeft; // 除以3是为了尽快找到合适的位置
                    while (cLeft + textWidth > pRight) {
                        cLeft--;
                    }
                    cTop = (int) (Math.random() * (rectHeight / 2)) + pTop;
                    while (cTop + textHeight > pBottom) {
                        cTop--;
                    }
                    cRight = cLeft + textWidth;
                    cBottom = cTop + textHeight;
                    showTags.add(new Tag(name, textSize, cLeft, cTop, cRight, cBottom));

                    //左
                    computeSingleRect(tags, --textSize, pLeft, pTop, cLeft, cBottom);
                    //上
                    computeSingleRect(tags, --textSize, cLeft, pTop, pRight, cTop);
                    //右
                    computeSingleRect(tags, --textSize, cRight, cTop, pRight, pBottom);
                    //下
                    computeSingleRect(tags, --textSize, pLeft, cBottom, cRight, pBottom);
                }
            }
        } else {
            //父布局高大于长，纵向布局合适
            int beforeTextSize = textSize;
            paint.setTextSize(textSize);
            textHeight = (int) (paint.getFontMetrics().bottom - paint.getFontMetrics().top);
            while (textHeight * name.length() > rectHeight) {
                textSize--;
                paint.setTextSize(textSize);
                textHeight = (int) (paint.getFontMetrics().bottom - paint.getFontMetrics().top);
            }
            if(textSize < MIN_TEXT_SIZE){
                return;
            }
            textWidth = (int) (paint.measureText(name) / name.length());
            int length = name.length();
            if (pLeft + textWidth > pRight) {
                //右 右边空间不足
                computeSingleRect(tags, --textSize, pLeft, pTop, pRight, pBottom);
                return;
            }
            for (int i = 0; i < length; i++) {
                cLeft = pLeft;
                cTop = pTop + i * textHeight;
                cRight = cLeft + textWidth;
                cBottom = cTop + textHeight;
                showTags.add(new Tag(String.valueOf(name.charAt(i)), textSize, cLeft, cTop, cRight, cBottom));
            }
            if (pRight - cRight > textWidth) {
                //右
                computeSingleRect(tags, beforeTextSize, cRight, pTop, pRight, cBottom);
                //下
                computeSingleRect(tags, --textSize, pLeft, cBottom, pRight, pBottom);
            } else {
                //右
                computeSingleRect(tags, --textSize, cRight, pTop, pRight, cBottom);
                //下
                computeSingleRect(tags, --textSize, pLeft, cBottom, pRight, pBottom);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d("---------->", showTags.size() + "个");
        for (Tag showTag : showTags) {
            Log.d("---------->", showTag.getName());
            paint.setTextSize(showTag.getTextsize());
//            canvas.drawRect(showTag.getLeft(), showTag.getTop(), showTag.getRight(), showTag.getBottom(), paint1);
            canvas.drawText(showTag.getName(), showTag.getLeft(), showTag.getBottom() - paint.getFontMetrics().bottom, paint);
        }
    }
}
