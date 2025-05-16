package ipana.renders.account;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import ipana.Ipana;
import ipana.eventapi.EventManager;
import ipana.events.EventTick;
import ipana.irc.user.UserProperties;
import ipana.utils.config.ConfigUtils;
import ipana.utils.font.FontHelper;
import ipana.utils.math.Pair;
import ipana.utils.render.EndPortalRenderer;
import ipana.utils.render.RenderUtils;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.minecraft_authenticator.login.User;
import net.hycrafthd.minecraft_authenticator.login.XBoxProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Session;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import pisi.unitedmeows.eventapi.event.listener.Listener;
import stelixobject.objectfile.SxfDataObject;
import stelixobject.objectfile.SxfFile;
import stelixobject.objectfile.reader.SXfReader;
import stelixobject.objectfile.writer.SxfWriter;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccountManager extends GuiScreen {
    private Button add = new Button("+", 15, 15);
    private Button remove = new Button("-", 15, 15);
    private Button edit = new Button("*", 15, 15);
    private Button modernAuth = new Button("Modern Auth", 65, 15);
    private Button openBrowser = new Button("Open Browser", 200, 15);
    private Button modernLogin = new Button("Login", 200, 15);
    private Minecraft mc = Minecraft.getMinecraft();
    private Pair<Session, User> loginDetails;

    private GuiTextField emailField = new GuiTextField(0,mc.fontRendererObj,0,0,100,15);
    private GuiTextField passField = new GuiTextField(1,mc.fontRendererObj,0,0,100,15);
    private GuiTextField authCode = new GuiTextField(2,mc.fontRendererObj,0,0,198,15);

    private List<Account> accounts = new ArrayList<>();
    private boolean editAlt;
    private boolean addAlt;
    private Account selected;
    private YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
    private YggdrasilUserAuthentication authentication = (YggdrasilUserAuthentication) service.createUserAuthentication(Agent.MINECRAFT);
    private String status = "";
    public FakeGoBrr lol;
    public Fakekekke player;
    private int yaw,prevYaw;
    private boolean scrollableLimit,scrollableStart;
    public int scroll;
    private boolean modernLoginScreen;

    private static final Color portalColor = new Color(5,5,5,100);
    private EndPortalRenderer portalRenderer = new EndPortalRenderer(4, new Color(132, 0, 255, 255), new Color(0, 0 ,0, 0));

    public AccountManager() {
        EventManager.eventSystem.subscribeAll(this);
        load();
        authCode.setMaxStringLength(64);
    }

    private Listener<EventTick> onTick = new Listener<>(event -> {
        prevYaw = yaw;
        yaw+=3;
        if (yaw >= 360) {
            yaw = 0;
        }
    });


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = RenderUtils.SCALED_RES;
        int gradient1 = new Color(225,225,225, 100).getRGB();
        int gradient2 = new Color(170,170,170, 100).getRGB();
        Color solidGray = new Color(150, 150, 150);
        Color solidLightGray = new Color(170, 170, 170, 200);
        Color alphaWhite = new Color(225,225,225,150);
        Color solidWhite = new Color(240,240,240);
        Color alphaDarkGray = new Color(100, 100, 100,150);
        Gui.drawRect(0,0, sr.getScaledWidth(), sr.getScaledHeight(), portalColor);
        portalRenderer.renderEndPortalEffect2D(0, 0, sr.getScaledWidth(), sr.getScaledHeight());
        RenderUtils.drawGradientRect(0,0,sr.getScaledWidth(),sr.getScaledHeight(),gradient1,gradient2);
        if (!accounts.isEmpty()) {
            scroll = Math.max(sr.getScaledHeight()-(accounts.size()+1)*25, scroll);
            scroll = Math.min(0, scroll);
            int accountY = 5+scroll;
            for (Account account : accounts) {
                if (selected == account) {
                    if (accountY + 20 <= sr.getScaledHeight() - 20) {
                        RenderUtils.drawFixedRect(4, accountY - 1, 201, accountY + 21, solidGray);
                    }
                    RenderUtils.drawFixedRect(205, 0, 206, sr.getScaledHeight(), solidLightGray);
                    Session session = getCurrentSession();
                    FontHelper.SIZE_18.drawStringWithShadow(status, 220, 10, Ipana.getClientColor().getRGB());
                    if (session != null) {
                        drawEntityOnScreen(375, 250, 100, prevYaw + (yaw-prevYaw)*partialTicks, 0, player);
                    }
                    int lX = 220;
                    int lW = 40;
                    int lY = sr.getScaledHeight() - 20;
                    int lH = 15;
                    if (session != null) {
                        FontHelper.SIZE_18.drawStringWithShadow("Name : " + session.getProfile().getName(), 220, lY - 30, Ipana.getClientColor().getRGB());
                        FontHelper.SIZE_18.drawStringWithShadow("UUID : " + session.getProfile().getId().toString(), 220, lY - 15, Ipana.getClientColor().getRGB());
                    }
                    RenderUtils.drawFixedRect(lX, lY, lX + lW, lY + lH, alphaWhite);
                    if (!modernLoginScreen && !editAlt && !addAlt && mouseX >= lX && mouseX <= lX + lW && mouseY >= lY && mouseY <= lY + lH) {
                        RenderUtils.drawFixedRect(lX, lY, lX + lW, lY + lH, new Color(Ipana.getClientColor().getRed(), Ipana.getClientColor().getGreen(), Ipana.getClientColor().getBlue(), 120));
                    }
                    FontHelper.SIZE_18.drawStringWithShadow("Login", lX + (lW / 2f - FontHelper.SIZE_18.getWidth("Login") / 2), lY + 4, Ipana.getClientColor().getRGB());
                }
                RenderUtils.drawFixedRect(5, accountY, 200, accountY + 20, solidWhite);
                if (mouseX >= 5 && mouseX <= 200 && mouseY >= accountY && mouseY <= accountY + 20 && mouseY < sr.getScaledHeight() - 25) {
                    RenderUtils.drawFixedRect(5, accountY, 200, accountY + 20, new Color(Ipana.getClientColor().getRed(), Ipana.getClientColor().getGreen(), Ipana.getClientColor().getBlue(), 120));
                }
                FontHelper.SIZE_18.drawStringWithShadow(account.getEmail(), 7, accountY + 2, Color.darkGray.getRGB());
                if (account.getPassword().isEmpty()) {
                    FontHelper.SIZE_12.drawStringWithShadow("§eNon Premium", 7, accountY + 12, Color.lightGray.getRGB());
                } else {
                    FontHelper.SIZE_18.drawStringWithShadow("*".repeat(account.getPassword().length()), 7, accountY + 14, Color.lightGray.getRGB());
                }

                accountY += 25;
            }
            int mouse = Mouse.getDWheel()/2;
            scroll += mouse;
        }
        RenderUtils.drawFixedRect(0,sr.getScaledHeight()-25,205,sr.getScaledHeight(),solidLightGray);
        add.render(5,sr.getScaledHeight()-20,mouseX,mouseY);
        remove.render(25,sr.getScaledHeight()-20,mouseX,mouseY);
        edit.render(45,sr.getScaledHeight()-20,mouseX,mouseY);
        modernAuth.render(65,sr.getScaledHeight()-20,mouseX,mouseY);
        if (modernLoginScreen) {
            RenderUtils.drawFixedRect(0,0,sr.getScaledWidth(),sr.getScaledHeight(),alphaDarkGray);
            authCode.xPosition = sr.getScaledWidth()/2-authCode.getWidth()/2 - 4;
            authCode.yPosition = sr.getScaledHeight()/2;
            openBrowser.render(sr.getScaledWidth()/2-(int)openBrowser.getWidth()/2, sr.getScaledHeight()/2-30, mouseX, mouseY);
            modernLogin.render(sr.getScaledWidth()/2-(int)modernLogin.getWidth()/2, sr.getScaledHeight()/2+20, mouseX, mouseY);
            FontHelper.SIZE_18.drawStringWithShadow("oAuthCode: ", authCode.xPosition, sr.getScaledHeight()/2f-10, Ipana.getClientColor().getRGB());
            authCode.drawTextBox();
            if (loginDetails != null) {
                FontHelper.SIZE_18.drawStringWithShadow("Name : " + loginDetails.first().getProfile().getName(), authCode.xPosition, sr.getScaledHeight()/2f+40, Ipana.getClientColor().getRGB());
                FontHelper.SIZE_18.drawStringWithShadow("UUID : " + loginDetails.first().getProfile().getId().toString(), authCode.xPosition, sr.getScaledHeight()/2f+52, Ipana.getClientColor().getRGB());
            }
        } else if (editAlt || addAlt) {
            RenderUtils.drawFixedRect(0,0,sr.getScaledWidth(),sr.getScaledHeight(),alphaDarkGray);
            emailField.xPosition = sr.getScaledWidth()/2-emailField.getWidth()/2;
            emailField.yPosition = sr.getScaledHeight()/2-30;
            passField.xPosition = sr.getScaledWidth()/2-passField.getWidth()/2;
            passField.yPosition = sr.getScaledHeight()/2;
            FontHelper.SIZE_18.drawStringWithShadow("Email",emailField.xPosition,emailField.yPosition-10,Ipana.getClientColor().getRGB());
            FontHelper.SIZE_18.drawStringWithShadow("Password",passField.xPosition,passField.yPosition-10,Ipana.getClientColor().getRGB());
            emailField.drawTextBox();
            passField.drawTextBox();
            //OK BUTTON
            {
                int bX = sr.getScaledWidth() / 2 - 47;
                int bW = 30;
                int bY = sr.getScaledHeight() / 2 + 50;
                int bH = 15;
                RenderUtils.drawFixedRect(bX, bY, bX + bW, bY + bH, alphaWhite);
                if (mouseX >= bX && mouseX <= bX + bW) {
                    if (mouseY >= bY && mouseY <= bY + bH) {
                        RenderUtils.drawFixedRect(bX, bY, bX + bW, bY + bH, new Color(Ipana.getClientColor().getRed(), Ipana.getClientColor().getGreen(), Ipana.getClientColor().getBlue(), 120));
                    }
                }
                FontHelper.SIZE_18.drawStringWithShadow("Ok", bX + (bW / 2f - FontHelper.SIZE_18.getWidth("Ok") / 2), bY + 4, Ipana.getClientColor().getRGB());
            }
            //CANCEL BUTTON

            {
                int bX = sr.getScaledWidth() / 2 + 15;
                int bW = 40;
                int bY = sr.getScaledHeight() / 2 + 50;
                int bH = 15;
                RenderUtils.drawFixedRect(bX, bY, bX + bW, bY + bH, alphaWhite);
                if (mouseX >= bX && mouseX <= bX + bW) {
                    if (mouseY >= bY && mouseY <= bY + bH) {
                        RenderUtils.drawFixedRect(bX, bY, bX + bW, bY + bH, new Color(Ipana.getClientColor().getRed(), Ipana.getClientColor().getGreen(), Ipana.getClientColor().getBlue(), 120));
                    }
                }
                FontHelper.SIZE_18.drawStringWithShadow("Cancel", bX + (bW / 2f - FontHelper.SIZE_18.getWidth("Cancel") / 2), bY + 4, Ipana.getClientColor().getRGB());
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void save() {
        SxfFile file = new SxfFile();
        SxfDataObject data = new SxfDataObject();
        for (final Account account : accounts) {
            SxfDataObject ersininAnnesi = new SxfDataObject();
            ersininAnnesi.variables().put("password",account.getPassword());
            data.objects().put(account.getEmail()+" ",ersininAnnesi);
        }
        file.base().put("Alts",data);
        new SxfWriter(file).write(ConfigUtils.getConfigFile("Alts.sxf").getAbsolutePath());
    }
    public void load() {
        SxfFile file = SXfReader.Read(ConfigUtils.getConfigFile("Alts.sxf").getAbsolutePath());
        if (file.base().isEmpty()) return;
        SxfDataObject data = file.get("Alts");
        for (Map.Entry<String,SxfDataObject> object : data.objects().entrySet()) {
            accounts.add(new Account(
                    object.getKey().replace(" ",""),
                    object.getValue().variable("password")));
        }
    }
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ScaledResolution sr = RenderUtils.SCALED_RES;
        if (modernAuth.isHovered(mouseX, mouseY) && !modernLoginScreen) {
            modernLoginScreen = true;
        }
        if (modernLoginScreen) {
            authCode.mouseClicked(mouseX, mouseY, mouseButton);
            if (openBrowser.isHovered(mouseX, mouseY)) {
                try {
                    openWebLink(Authenticator.microsoftLogin().toURI());
                } catch (URISyntaxException e) {

                }
            }
            if (modernLogin.isHovered(mouseX, mouseY)) {
                Pair<Session, User> pair = createSession(authCode.getText());
                mc.session = pair.first();
                this.loginDetails = pair;
                //modernLoginScreen = false;
            }
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }
        if (edit.isHovered(mouseX, mouseY) && !editAlt) {
            editAlt = true;
            if (selected != null) {
                emailField.setText(selected.getEmail());
                passField.setText(selected.getPassword());
            }
        }
        if (add.isHovered(mouseX, mouseY) && !addAlt) {
            addAlt = true;
            emailField.setText("");
            passField.setText("");
        }
        if (remove.isHovered(mouseX, mouseY) && selected != null) {
            accounts.remove(selected);
            if (!accounts.isEmpty()) {
                selected = accounts.get(0);
            }
        }
        if (addAlt || editAlt) {
            emailField.mouseClicked(mouseX, mouseY, mouseButton);
            passField.mouseClicked(mouseX, mouseY, mouseButton);
            boolean okHovered = mouseX >= sr.getScaledWidth_double() / 2 - 47 && mouseX <= sr.getScaledWidth_double() / 2 - 17 && mouseY >= sr.getScaledHeight_double() / 2 + 50 && mouseY <= sr.getScaledHeight_double() / 2 + 65;
            boolean cancelHovered = mouseX >= sr.getScaledWidth_double() / 2 + 15 && mouseX <= sr.getScaledWidth_double() / 2 + 55 && mouseY >= sr.getScaledHeight_double() / 2 + 50 && mouseY <= sr.getScaledHeight_double() / 2 + 65;
            if (okHovered) {
                if (emailField.getText().isEmpty() && passField.getText().isEmpty()) {
                    emailField.setText("can not be blank");
                    passField.setText("can not be blank");
                } else {

                    if (emailField.getText().isEmpty()) {
                        emailField.setText("can not be blank");
                    } else if (emailField.getText().contains("@") || passField.getText().isEmpty()) {
                        if (addAlt) {
                            selected = new Account(emailField.getText(), passField.getText());
                            accounts.add(selected);
                        } else {
                            if (selected != null) {
                                selected.setEmail(emailField.getText());
                                selected.setPassword(passField.getText());
                            }
                        }
                        editAlt = addAlt = false;
                    }

                }
            } else if (cancelHovered) {
                editAlt = addAlt = false;
            }
        } else {
            int accountY = 5 + scroll;
            for (Account account : accounts) {
                if (mouseX >= 5 && mouseX <= 205 && mouseY >= accountY && mouseY <= accountY + 20 && mouseY < sr.getScaledHeight() - 25) {
                    selected = account;
                }
                accountY += 25;
            }
            if (selected != null) {
                int lX = 220;
                int lW = 40;
                int lY = sr.getScaledHeight() - 20;
                int lH = 15;
                if (mouseX >= lX && mouseX <= lX + lW && mouseY >= lY && mouseY <= lY + lH) {
                    if (authentication.isLoggedIn()) {
                        authentication.logOut();
                    }
                    if (selected.getPassword().isEmpty()) {
                        mc.session = new Session(selected.getEmail(), selected.getEmail(), "0", "legacy");
                        status = "§eNon Premium " + selected.getEmail();
                    } else {
                        mc.session = createOldSession(selected.getEmail(), selected.getPassword());
                        Session check = getCurrentSession();
                        if (check != null) {
                            lol = new FakeGoBrr(mc, check.getProfile());
                            WorldButFake world = new WorldButFake(lol, new WorldSettings(0L, WorldSettings.GameType.SURVIVAL, false, false, WorldType.DEFAULT));
                            player = new Fakekekke(lol, world, check.getProfile());

                            status = "§aSuccessfully Logged In " + check.getProfile().getName();
                        } else {
                            status = "§cFailed To Login";
                        }
                    }
                    Ipana.mainIRC().self().setAndSendProperty(UserProperties.INGAME_NAME, mc.session.getUsername());
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!modernLoginScreen) {
            if (addAlt || editAlt) {
                if (emailField.isFocused()) {
                    emailField.textboxKeyTyped(typedChar, keyCode);
                }
                if (passField.isFocused()) {
                    passField.textboxKeyTyped(typedChar, keyCode);
                }
            }
        } else {
            authCode.textboxKeyTyped(typedChar, keyCode);
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            save();
        }
        if (!modernLoginScreen) {
            super.keyTyped(typedChar, keyCode);
        } else if (keyCode == Keyboard.KEY_ESCAPE) {
            authCode.setText("");
            loginDetails = null;
            modernLoginScreen = false;
        }
    }

    private Pair<Session, User> createSession(String oAuthCode) {
        System.out.println("Auth code: " + oAuthCode);

        final Authenticator authenticator = Authenticator.ofMicrosoft(oAuthCode) //
                .serviceConnectTimeout(5000) //
                .serviceReadTimeout(10000) //
                .shouldAuthenticate() //
                //.shouldRetrieveXBoxProfile() //
                .build();


        try {
            authenticator.run();
        } catch (net.hycrafthd.minecraft_authenticator.login.AuthenticationException ex) {
            ex.printStackTrace(System.out);
            System.out.println("Updated auth file: " + authenticator.getResultFile());
        }

        final User user = authenticator.getUser().get();
        System.out.println(user);
        return Pair.of(new Session(user.name(), user.uuid(), user.accessToken(), "legacy"), user);
    }
    private Session createOldSession(String email, String pass) {
        authentication.setUsername(email);
        authentication.setPassword(pass);
        try
        {
            authentication.logIn();
            return new Session(authentication.getSelectedProfile().getName(), authentication.getSelectedProfile().getId().toString(), authentication.getAuthenticatedToken(), "legacy");
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        return null;
    }
    private Session getCurrentSession() {
        Session session = null;
        if (authentication.getSelectedProfile() != null) {
            session = new Session(authentication.getSelectedProfile().getName(), authentication.getSelectedProfile().getId().toString(), authentication.getAuthenticatedToken(), "legacy");
        }
        return session;
    }

    public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.enableDepth();
        GlStateManager.translate((float)posX, (float)posY, 50.0F);
        GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float)Math.atan((mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        ent.renderYawOffset = (mouseX / 40.0F) * 40.0F;
        ent.rotationYaw =  ((mouseX / 40.0F)) * 40.0F;
        ent.rotationPitch = -(((mouseY / 40.0F))) * 20.0F;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rendermanager.setRenderShadow(true);
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        GlStateManager.disableDepth();
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public static class Button {
        private String name;
        private float width;
        private float height;
        private int renderingX,renderingY;

        public Button(String name, float width,float height) {
            this.name = name;
            this.width = width;
            this.height = height;
        }

        public boolean isHovered(int mouseX,int mouseY) {
            return mouseX >= renderingX && mouseX <= renderingX+width && mouseY >= renderingY && mouseY <= renderingY+height;
        }

        public void render(int x, int y, int mouseX, int mouseY) {
            renderingX = x;
            renderingY = y;
            RenderUtils.drawFixedRect(x,y,x+width,y+height,new Color(225,225,225));
            if (isHovered(mouseX, mouseY)) {
                RenderUtils.drawFixedRect(x,y,x+width,y+height,new Color(Ipana.getClientColor().getRed(),Ipana.getClientColor().getGreen(),Ipana.getClientColor().getBlue(),120));
            }
            FontHelper.SIZE_18.drawStringWithShadow(getName(),x+((width/2) - (FontHelper.SIZE_18.getWidth(getName())/2)),y+4, Ipana.getClientColor().getRGB());
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }
    }
}
