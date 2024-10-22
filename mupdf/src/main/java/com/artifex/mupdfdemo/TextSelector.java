package com.artifex.mupdfdemo;

import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

/*
class TextSelector {
    final private TextWord[][] mText;
    final private RectF mSelectBox;

    public TextSelector(TextWord[][] text, RectF selectBox) {
        mText = text;
        mSelectBox = selectBox;
    }
    private float mStartLimit = Float.NEGATIVE_INFINITY;
    private float mEndLimit = Float.POSITIVE_INFINITY;

    public TextSelector(TextWord[][] text, RectF selectBox, float startLimit, float endLimit) {
        mStartLimit = startLimit;
        mEndLimit = endLimit;
        mText = text;
        mSelectBox = selectBox;
    }

    public void select(TextProcessor tp) {
        if (mText == null || mSelectBox == null)
            return;
        Log.d("selctnew",""+mSelectBox.left);
        ArrayList<TextWord[]> lines = new ArrayList<TextWord[]>();
        for (TextWord[] line : mText)
            if (line[0].bottom > mSelectBox.top && line[0].top < mSelectBox.bottom)
                lines.add(line);

        Iterator<TextWord[]> it = lines.iterator();
        while (it.hasNext()) {
            TextWord[] line = it.next();
            boolean firstLine = line[0].top < mSelectBox.top;
            boolean lastLine = line[0].bottom > mSelectBox.bottom;
            float start = Float.NEGATIVE_INFINITY;
            float end = Float.POSITIVE_INFINITY;

            if (firstLine && lastLine) {
                start = Math.min(mSelectBox.left, mSelectBox.right);
                end = Math.max(mSelectBox.left, mSelectBox.right);
            } else if (firstLine) {
                start = mSelectBox.left;
            } else if (lastLine) {
                end = mSelectBox.right;

            }
            tp.onStartLine();
            for (TextWord word : line)
                if (word.right > start && word.left < end)
                    tp.onWord(word);

            tp.onEndLine();

        }
    }
}
*/

class TextSelector
{
    final private TextWord[][] mText;
    final private RectF mSelectBox;

    private float mStartLimit = Float.NEGATIVE_INFINITY;
    private float mEndLimit = Float.POSITIVE_INFINITY;

    public TextSelector(TextWord[][] text, RectF selectBox) {
        mText = text;
        mSelectBox = selectBox;
    }

    public TextSelector(TextWord[][] text, RectF selectBox, float startLimit, float endLimit) {
        mStartLimit = startLimit;
        mEndLimit = endLimit;
        mText = text;
        mSelectBox = selectBox;
    }

    public void select(TextProcessor tp) {
        if (mText == null || mSelectBox == null)
            return;

        ArrayList<TextWord[]> lines = new ArrayList<TextWord[]>();
        for (TextWord[] line : mText)
            if (line[0].bottom > mSelectBox.top && line[0].top < mSelectBox.bottom)
                lines.add(line);

        Iterator<TextWord[]> it = lines.iterator();
        while (it.hasNext()) {
            TextWord[] line = it.next();
            boolean firstLine = line[0].top < mSelectBox.top;
            boolean lastLine = line[0].bottom > mSelectBox.bottom;

            float start = mStartLimit;
            float end = mEndLimit;

            if (firstLine && lastLine) {
                start = Math.min(mSelectBox.left, mSelectBox.right);
                end = Math.max(mSelectBox.left, mSelectBox.right);
            } else if (firstLine) {
                start = mSelectBox.left;
            } else if (lastLine) {
                end = mSelectBox.right;
            }

            tp.onStartLine();

            for (TextWord word : line)
            {
                if (word.right > start && word.left < end)
                    tp.onWord(word);
            }

            tp.onEndLine();
        }
        tp.onEndText();
    }
}

