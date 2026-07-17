package io.github.some_example_name.Ingame.Others;

public class TextboxLayout {
    public float x;
    public float y;
    public String align;

    public TextboxLayout() {} // Required for JSON parsing

    public TextboxLayout(float x, float y, String align) {
        this.x = x;
        this.y = y;
        this.align = align;
    }
}

