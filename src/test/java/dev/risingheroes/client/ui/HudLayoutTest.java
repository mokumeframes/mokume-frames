package dev.risingheroes.client.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class HudLayoutTest {
    @ParameterizedTest @CsvSource({"320,240","426,240","640,360","960,540","853,360","1280,540"})
    void scaledPanelsStayInsideEveryEdgeAndCanBeDraggedBack(int width,int height) {
        for(boolean compact:new boolean[]{false,true})for(boolean numbers:new boolean[]{false,true})for(double scale:new double[]{.6,1,1.5})for(double anchor:new double[]{0,.5,1}) {
            var l=HudLayout.at(width,height,scale,anchor,anchor,compact,numbers);
            assertTrue(l.x()>=8&&l.y()>=8);assertTrue(l.x()+l.width()<=width-8&&l.y()+l.height()<=height-64,"The survival band must stay clear");
            var roundTrip=HudLayout.at(width,height,scale,HudLayout.anchor(l.x(),width,l.width()),HudLayout.anchor(l.y(),height,l.height(),56),compact,numbers);
            assertEquals(l,roundTrip,"Resize/drag round trip must not shift the panel");
        }
    }
    @Test void editorPreviewFitsBetweenItsControlsAndLargeSurvivalRowsStayClear() {
        for(int viewportHeight:new int[]{240,270,360,540}) {
            var preview=HudLayout.at(320,viewportHeight-112,1.5,1,1,false,true,0);
            assertTrue(preview.y()+76+preview.height()<=viewportHeight-44);
            var mounted=HudLayout.at(320,viewportHeight,1.5,0,1,false,true,86);
            assertTrue(mounted.y()+mounted.height()<=viewportHeight-94);
        }
    }
    @Test void draggingBeyondScreenClampsAndInvalidNumbersCannotReachRenderer() {
        assertEquals(0,HudLayout.anchor(-100,320,90));assertEquals(1,HudLayout.anchor(1000,320,90));
        assertThrows(IllegalArgumentException.class,()->HudLayout.at(320,240,Double.NaN,0,1,false,true));
        assertThrows(IllegalArgumentException.class,()->HudLayout.at(320,240,1,Double.NaN,1,false,true));
    }
}
