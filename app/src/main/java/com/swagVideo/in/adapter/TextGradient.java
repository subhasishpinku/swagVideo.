package com.swagVideo.in.adapter;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

public class TextGradient extends CharacterStyle implements UpdateAppearance {
    private int startColor;
    private int endColor;
    private int lineHeight;

    public TextGradient(int startColor, int endColor, int lineHeight) {
        this.startColor = startColor;
        this.endColor = endColor;
        this.lineHeight = lineHeight;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
       /* tp.setShader(new LinearGradient(0, 0, 0, lineHeight,
                startColor, endColor, Shader.TileMode.REPEAT));*/
        tp.setShader(new LinearGradient(0, 0, 100, 160, new int[]{
                startColor,
                endColor}
        ,null, Shader.TileMode.CLAMP));
    }


}