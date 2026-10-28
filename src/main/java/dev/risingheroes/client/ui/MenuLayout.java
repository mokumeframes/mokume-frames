package dev.risingheroes.client.ui;

/** Responsive geometry is pure Java so it can be checked without opening a game window. */
public record MenuLayout(int left,int titleTop,int menuTop,int buttonWidth,int buttonHeight,int gap) {
    public static MenuLayout at(int width,int height) {
        int h=height<300?18:24,gap=height<300?4:6;
        return new MenuLayout(Math.max(16,width/20),Math.max(14,height/9),Math.max(78,Math.min(height/2-22,height-5*(h+gap)-20)),Math.max(130,Math.min(216,width/3)),h,gap);
    }
    public int row(int n){return menuTop+n*(buttonHeight+gap);}
}
