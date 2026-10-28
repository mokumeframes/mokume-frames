package dev.risingheroes.client.ui;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class MenuLayoutTest {
    @ParameterizedTest @CsvSource({"320,240","426,240","640,360","960,540","853,360","1280,540"})
    void menuFitsSupportedGuiSizes(int width,int height){var l=MenuLayout.at(width,height);assertTrue(l.left()>=12);assertTrue(l.left()+l.buttonWidth()<=width);assertTrue(l.menuTop()>l.titleTop()+38);assertTrue(l.row(4)+l.buttonHeight()<=height-12);}
}
