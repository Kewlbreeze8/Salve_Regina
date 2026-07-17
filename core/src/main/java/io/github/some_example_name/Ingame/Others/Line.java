package io.github.some_example_name.Ingame.Others;

public class Line {
    public String type = "text";          // "dialogue", "text", etc.
    public String text;                   // Line text
    public String speaker;               // Optional speaker
    public Action action;                // Optional inline action

    public Line() {} // Required for JSON deserialization

    public Line(String type, String text, String speaker, Action action) {
        this.type = type != null ? type : "text";
        this.text = text;
        this.speaker = speaker;
        this.action = action;
    }
}
