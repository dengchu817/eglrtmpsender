package com.example.dengchu.eglrtmpsender.Filter;

import android.content.res.Resources;

/**
 * Created by dengchu on 2018/6/6.
 */

public class LazyFilter extends BaseFilter {
    public LazyFilter(Resources resources){
        super(resources, "shader/base.vert","shader/base.frag");
    }

    public LazyFilter(String vert,String frag){
        super(null, vert, frag);
    }

    public LazyFilter(){
        super(null,"attribute vec4 aVertexCo;\n" +
                        "attribute vec2 aTextureCo;\n" +
                        "\n" +
                        "uniform mat4 uVertexMatrix;\n" +
                        "uniform mat4 uTextureMatrix;\n" +
                        "\n" +
                        "varying vec2 vTextureCo;\n" +
                        "\n" +
                        "void main(){\n" +
                        "    gl_Position = uVertexMatrix*aVertexCo;\n" +
                        "    vTextureCo = (uTextureMatrix*vec4(aTextureCo,0,1)).xy;\n" +
                        "}",
                "precision mediump float;\n" +
                        "varying vec2 vTextureCo;\n" +
                        "uniform sampler2D uTexture;\n" +
                        "void main() {\n" +
                        "    gl_FragColor = texture2D( uTexture, vTextureCo);\n" +
                        "}");
    }
}
