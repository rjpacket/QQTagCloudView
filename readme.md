### 一、QQ效果与最终效果比较

![qq_5.png](https://upload-images.jianshu.io/upload_images/5994029-cbc9c7e578608b9c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![qq_6.png](https://upload-images.jianshu.io/upload_images/5994029-6b10989103236600.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 二、分析

从效果大致可以看出两个规律：

> 1. 字体的矩形面积越来越小
> 2. 字体大小越来越小

很像废话吧，不是的。除了字体，我们还能看到文字有竖向排行有横向排列，而且没有规律。

#### 2.1 问题分解

假设我们只有一个标签文字，可以选择自定义`View`(当然可以选择自定义ViewGroup)，然后随机标签文字的`left`和`top`，文字大小从30sp开始，然后在`onDraw`里面绘制矩形，在矩形里面绘制文字。

绘制第一个标签文字之后，我们想绘制第二个标签文字，如果我们还在当前的`View`里面去随机一个`Rect`，可能会和第一个标签重合，那怎么办？我们想到了裁剪，看下图：

![qq_1.png](https://upload-images.jianshu.io/upload_images/5994029-171a6df7e9334da2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

沿着标签我们可以将`View`切成`Rect`①、`Rect`②、`Rect`③、`Rect`④，这个时候我们分别将四块矩形看成新的`View`去绘制一个标签文字。

这样大问题就化解成了许许多多的小问题。

#### 2.2 如果`Rect`宽大于高

1. 如果标签文字的高度大于`Rect`的高度，我们可以递减标签文字的`TextSize`，一直到标签文字的高度小于`Rect`的高度，我们直接从`Rect`的`Left`开始绘制标签就可以，看图：

![qq_2.png](https://upload-images.jianshu.io/upload_images/5994029-c90b068deb897773.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

第一个标签绘制完成之后，继续在这个标签的右边重复绘制第一个标签大小的标签，一直到`Rect`剩余的空间不足以绘制一个当前的大小的标签。

2. 如果文字的宽度大于`Rect`的宽度，同样的我们也递减标签文字的`TextSize`，一直到标签文字的宽度小于`Rect`的宽度，我们直接从`Rect`的`Top`开始绘制标签就可以，看图：

![qq_3.png](https://upload-images.jianshu.io/upload_images/5994029-99c8b6ff6212b956.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

第一个标签绘制完成之后，继续在这个标签的下边重复绘制第一个标签大小的标签，一直到`Rect`剩余的空间不足以绘制一个当前的大小的标签。

3. 如果以上都不满足，说明标签的宽高都远小于`Rect`的宽高，那就变成了我们最开始的大问题，随机标签文字的`left`和`top`，再切四个`Rect`出来，重复以上步骤。

#### 2.3 如果`Rect`高大于宽

`Rect`高大于宽，标签适合竖向排列，竖向排列考虑起来比较简单，不需要随机一个位置开始竖向，就从`Rect`的`Left`开始排列，看起来整齐，看图：

![qq_4.png](https://upload-images.jianshu.io/upload_images/5994029-df914399757ba1a7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

第一个标签绘制完成之后，继续在标签的右边重复绘制第一个标签大小的标签，一直到`Rect`右边剩余的空间不足以绘制一个当前的大小的标签，然后将剩下的空间切成`Rect`①和`Rect`②，重复以上步骤。

### 三、核心代码

#### 3.1 定义Tag对象

```
public class Tag {
    private String name;
    private int left;
    private int top;
    private int right;
    private int bottom;
    private int textsize;

    // 省略构造函数和setter getter
}
```

这个`class`的作用类似记录器，记录每一个`Tag`的位置和文字大小信息。

#### 3.2 核心函数

```
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
```

很清楚的看到，是一个递归函数，一开始就是递归的结束条件。注意里面的切割`Rect`的方法，`pLeft`、`pTop`、`pRight`、`pBottom`代表父`Rect`的边界，`cLeft`、`cTop`、`cRight`、`cBottom`代表`Tag`的边界。里面有一个很巧妙的记录进入条件时候的`TextSize`，目的是让下一次递归还继续进入这个条件下，也就做到了重复绘制相同大小的`Tag`的目的。

但是在`textWidth >= rectWidth`这个条件下记录`TextSize`却容易造成最后一个`Tag`绘制不出来，导致留白区域大，有一点瑕疵，但是整体目的达到了。

附上[简书地址](https://www.jianshu.com/p/30e91428e384)，喜欢的给个`Star`，谢谢。