package dev.risingheroes.client.ui;

/** Coordinates include the whole panel, so scale and dragging never clip its header or meters. */
public record HudLayout(int x,int y,int width,int height,int cell,float scale,boolean numbers) {
    public static final int MARGIN=8;
    public static HudLayout at(int viewportWidth,int viewportHeight,double requestedScale,double anchorX,double anchorY,boolean compact,boolean numbers) {
        return at(viewportWidth,viewportHeight,requestedScale,anchorX,anchorY,compact,numbers,56);
    }
    public static HudLayout at(int viewportWidth,int viewportHeight,double requestedScale,double anchorX,double anchorY,boolean compact,boolean numbers,int bottomInset) {
        if(viewportWidth<160||viewportHeight<40||bottomInset<0||!Double.isFinite(requestedScale)||!Double.isFinite(anchorX)||!Double.isFinite(anchorY))throw new IllegalArgumentException("Invalid HUD viewport or settings");
        bottomInset=Math.min(bottomInset,viewportHeight-2*MARGIN-20);
        int cell=compact?34:38;
        int logicalWidth=3*cell+16,logicalHeight=22+2*(cell+4)+13+3*(numbers?16:7)+5;
        float scale=(float)Math.min(Math.max(.6,Math.min(1.5,requestedScale)),Math.min((viewportWidth-2.0*MARGIN)/logicalWidth,(viewportHeight-bottomInset-2.0*MARGIN)/logicalHeight));
        int width=(int)Math.ceil(logicalWidth*scale),height=(int)Math.ceil(logicalHeight*scale);
        int x=MARGIN+(int)Math.round(clamp(anchorX)*(viewportWidth-2*MARGIN-width));
        int y=MARGIN+(int)Math.round(clamp(anchorY)*(viewportHeight-bottomInset-2*MARGIN-height));
        return new HudLayout(x,y,width,height,cell,scale,numbers);
    }
    public static double anchor(int desiredPosition,int viewportSize,int panelSize) {
        return anchor(desiredPosition,viewportSize,panelSize,0);
    }
    public static double anchor(int desiredPosition,int viewportSize,int panelSize,int endInset) {
        int range=viewportSize-2*MARGIN-panelSize-endInset;return range<=0?0:clamp((desiredPosition-MARGIN)/(double)range);
    }
    private static double clamp(double value){return Math.max(0,Math.min(1,value));}
    public boolean contains(double mouseX,double mouseY){return mouseX>=x&&mouseX<x+width&&mouseY>=y&&mouseY<y+height;}
}
