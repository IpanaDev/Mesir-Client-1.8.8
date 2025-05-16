package ipana.renders.ide.panels.pack;

import ipana.renders.ide.panels.Screen;
import ipana.renders.ide.panels.pack.item.Package;
import ipana.utils.font.FontHelper;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class CreateItemScreen extends Screen {

    public CreateItemScreen(float x, float y, Package path) {
        super(x, y, new Element[]{new Element("New Package"),new Element("New Class")});
        textField = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj,(int)x-1,(int)y+22,(int) getWidth()+2,12);
        textField.setCanLoseFocus(false);
        textField.setFocused(true);
        this.path = path;
    }
    private Element selectedElement;
    private GuiTextField textField;
    private Package path;
    private ItemType itemType;

    @Override
    public void draw(int mouseX, int mouseY) {
        super.draw(mouseX, mouseY);
        float y = getY();
        for (Element element : getElements()) {
            if (element == selectedElement) {
                RenderUtils.drawFixedRect(getX(),y,getX()+getWidth(),y+10, Color.pink);
            }
            FontHelper.SIZE_18.drawString(element.getName(),getX(),y+2,Color.white.getRGB());
            y+=10;
        }
        if (selectedElement != null) {
            RenderUtils.drawFixedRect(textField.xPosition-1,textField.yPosition-1,textField.xPosition+Math.max(FontHelper.SIZE_18.getWidth(textField.getText())+1,getWidth())+3,textField.yPosition+13, Color.black);
            RenderUtils.drawFixedRect(textField.xPosition,textField.yPosition,textField.xPosition+Math.max(FontHelper.SIZE_18.getWidth(textField.getText())+1,getWidth())+2,textField.yPosition+12, Color.lightGray);
            FontHelper.SIZE_18.drawString(textField.getText(),textField.xPosition,textField.yPosition+2,Color.black.getRGB());
        }
    }

    @Override
    public void onClick(int mouseX, int mouseY, int button) {
        if (button == 0) {
            double y = getY();
            boolean isClicked = false;
            for (Element element : getElements()) {
                if (isHovered(mouseX, mouseY, y, 10)) {
                    if (element.getName().equalsIgnoreCase("new class")) {
                        itemType = ItemType.Class;
                    } else {
                        itemType = ItemType.Package;
                    }
                    selectedElement = element;
                    isClicked = true;
                }
                y += 10;
            }
            if (!isClicked) {
                setShouldRemove(true);
            }
        }
    }

    @Override
    public void onPress(int key, char typedChar) {
        textField.textboxKeyTyped(typedChar,key);
        if (key == Keyboard.KEY_RETURN) {
            if (textField.getText().replace(" ","").equalsIgnoreCase("")) {
                //TODO: Can't be blank
            } else {
                selectedElement = null;
                setShouldRemove(true);
                String name = textField.getText().replace(" ","");
                if (itemType == ItemType.Package) {
                    PackagePanel.INSTANCE.addPackage(path.addChild(name));
                } else if (itemType == ItemType.Class) {
                    path.addClass(name);
                }
            }
        }
    }

    private enum ItemType {
        Package,Class
    }
}
