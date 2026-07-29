package com.restaurant.pos.ui.theme;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.Icon;
import java.awt.Color;

public final class Icons {

    private Icons() {}

    public static Icon store(Color color, int size) {
        return svg("store", color, size);
    }

    public static Icon brand(Color color, int size) {
        return store(color, size);
    }

    public static Icon dashboard(Color color, int size) {
        return svg("dashboard", color, size);
    }

    public static Icon orders(Color color, int size) {
        return svg("orders", color, size);
    }

    public static Icon menu(Color color, int size) {
        return svg("menu", color, size);
    }

    public static Icon reports(Color color, int size) {
        return svg("reports", color, size);
    }

    public static Icon settings(Color color, int size) {
        return svg("settings", color, size);
    }

    public static Icon users(Color color, int size) {
        return svg("users", color, size);
    }

    public static Icon logout(Color color, int size) {
        return svg("logout", color, size);
    }

    public static Icon backspace(Color color, int size) {
        return svg("backspace", color, size);
    }

    public static Icon search(Color color, int size) {
        return svg("search", color, size);
    }

    public static Icon plus(Color color, int size) {
        return svg("plus", color, size);
    }

    public static Icon edit(Color color, int size) {
        return svg("edit", color, size);
    }

    public static Icon trash(Color color, int size) {
        return svg("trash", color, size);
    }

    public static Icon userPlus(Color color, int size) {
        return svg("user-plus", color, size);
    }

    public static Icon key(Color color, int size) {
        return svg("key", color, size);
    }

    public static Icon checkCircle(Color color, int size) {
        return svg("check-circle", color, size);
    }

    public static Icon xCircle(Color color, int size) {
        return svg("x-circle", color, size);
    }

    public static Icon alertTriangle(Color color, int size) {
        return svg("alert-triangle", color, size);
    }

    public static Icon printer(Color color, int size) {
        return svg("printer", color, size);
    }

    public static Icon download(Color color, int size) {
        return svg("download", color, size);
    }

    public static Icon fileText(Color color, int size) {
        return svg("file-text", color, size);
    }

    public static Icon refresh(Color color, int size) {
        return svg("refresh", color, size);
    }

    public static Icon toggleOn(Color color, int size) {
        return svg("toggle-on", color, size);
    }

    public static Icon toggleOff(Color color, int size) {
        return svg("toggle-off", color, size);
    }

    public static Icon camera(Color color, int size) {
        return svg("camera", color, size);
    }

    public static Icon image(Color color, int size) {
        return svg("image", color, size);
    }

    public static Icon calendar(Color color, int size) {
        return svg("calendar", color, size);
    }

    public static Icon user(Color color, int size) {
        return svg("user", color, size);
    }

    private static Icon svg(String name, Color color, int size) {
        FlatSVGIcon icon = new FlatSVGIcon("icons/lucide/" + name + ".svg", size, size);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> color));
        return icon;
    }
}
