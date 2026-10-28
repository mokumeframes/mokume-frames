package dev.risingheroes.client.ui;

import dev.risingheroes.MhaConfig;
import dev.risingheroes.client.CombatHud;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Changes are local until Save; Escape/Cancel discard the draft. */
public final class HudEditorScreen extends Screen {
    private final Screen parent;
    private double scale=MhaConfig.HUD_SCALE.get(),opacity=MhaConfig.HUD_OPACITY.get(),anchorX=MhaConfig.HUD_ANCHOR_X.get(),anchorY=MhaConfig.HUD_ANCHOR_Y.get();
    private boolean compact=MhaConfig.HUD_COMPACT.get(),numbers=MhaConfig.HUD_NUMBERS.get(),tracker=MhaConfig.QUEST_TRACKER.get(),dragging;
    private double grabX,grabY;
    public HudEditorScreen(Screen parent){super(Component.translatable("mha.menu.hudEditor"));this.parent=parent;}
    private HudLayout layout(){var p=HudLayout.at(width,Math.max(40,height-112),scale,anchorX,anchorY,compact,numbers,0);return new HudLayout(p.x(),p.y()+76,p.width(),p.height(),p.cell(),p.scale(),p.numbers());}
    @Override protected void init() {
        int x=12,w=(width-32)/3;
        addRenderableWidget(new HeroButton(x,29,w,18,Component.literal(String.format(Locale.ROOT,"Scale: %.1f",scale)),b->{scale=scale>=1.49?.6:Math.round((scale+.1)*10)/10.0;rebuildWidgets();},false));
        addRenderableWidget(new HeroButton(x+w+4,29,w,18,Component.literal(String.format(Locale.ROOT,"Opacity: %.0f%%",opacity*100)),b->{opacity=opacity>=.99?.3:Math.round((opacity+.1)*10)/10.0;rebuildWidgets();},false));
        addRenderableWidget(new HeroButton(x+2*(w+4),29,w,18,Component.literal("Compact: "+(compact?"on":"off")),b->{compact=!compact;rebuildWidgets();},false));
        addRenderableWidget(new HeroButton(x,51,w,18,Component.literal("Numbers: "+(numbers?"on":"off")),b->{numbers=!numbers;rebuildWidgets();},false));
        addRenderableWidget(new HeroButton(x+w+4,51,w,18,Component.literal("Objectives: "+(tracker?"on":"off")),b->{tracker=!tracker;rebuildWidgets();},false));
        addRenderableWidget(new HeroButton(x+2*(w+4),51,w,18,Component.literal("Move to "+(anchorX<.5?"right":"left")),b->{anchorX=anchorX<.5?1:0;anchorY=1;},false));
        addRenderableWidget(new HeroButton(x,height-28,w,18,Component.translatable("gui.cancel"),b->onClose(),false));
        addRenderableWidget(new HeroButton(x+w+4,height-28,w,18,Component.translatable("controls.reset"),b->{scale=1;opacity=.9;anchorX=0;anchorY=1;compact=false;numbers=true;tracker=true;rebuildWidgets();},false));
        addRenderableWidget(new HeroButton(x+2*(w+4),height-28,w,18,Component.translatable("mha.menu.saveHud"),b->save(),true));
    }
    private void save() {
        MhaConfig.HUD_SCALE.set(scale);MhaConfig.HUD_OPACITY.set(opacity);MhaConfig.HUD_ANCHOR_X.set(anchorX);MhaConfig.HUD_ANCHOR_Y.set(anchorY);
        MhaConfig.HUD_COMPACT.set(compact);MhaConfig.HUD_NUMBERS.set(numbers);MhaConfig.QUEST_TRACKER.set(tracker);MhaConfig.CLIENT.save();onClose();
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt) {
        g.fill(0,0,width,height,0xa0111b27);
        var l=layout();CombatHud.drawPanel(g,l,opacity,true);g.renderOutline(l.x()-2,l.y()-2,l.width()+4,l.height()+4,Theme.TEAL);
        g.fill(0,0,width,77,0xf0111b27);g.fill(0,height-36,width,height,0xf0111b27);
        g.drawString(font,"HUD LAYOUT / Drag panel or Alt + arrows",12,12,Theme.PAPER,false);
        if(height>=300)g.drawCenteredString(font,"Preview values / Vanilla survival displays remain active",width/2,85,Theme.MUTED);
        super.render(g,mx,my,dt);
    }
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float dt){}
    @Override public boolean mouseClicked(double x,double y,int button) {
        if(super.mouseClicked(x,y,button))return true;
        var l=layout();if(button==0&&y>77&&y<height-36&&l.contains(x,y)){dragging=true;grabX=x-l.x();grabY=y-l.y();return true;}return false;
    }
    @Override public boolean mouseDragged(double x,double y,int button,double dx,double dy) {
        if(dragging&&button==0){var l=layout();anchorX=HudLayout.anchor((int)Math.round(x-grabX),width,l.width());anchorY=HudLayout.anchor((int)Math.round(y-grabY)-76,Math.max(40,height-112),l.height());return true;}return super.mouseDragged(x,y,button,dx,dy);
    }
    @Override public boolean mouseReleased(double x,double y,int button){if(button==0)dragging=false;return super.mouseReleased(x,y,button);}
    @Override public boolean keyPressed(int key,int scanCode,int modifiers) {
        if(hasAltDown()&&key>=262&&key<=265) {
            var l=layout();int step=hasShiftDown()?1:8;
            anchorX=HudLayout.anchor(l.x()+(key==262?step:key==263?-step:0),width,l.width());
            anchorY=HudLayout.anchor(l.y()-76+(key==264?step:key==265?-step:0),Math.max(40,height-112),l.height());return true;
        }
        return super.keyPressed(key,scanCode,modifiers);
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return parent!=null&&parent.isPauseScreen();}
}
