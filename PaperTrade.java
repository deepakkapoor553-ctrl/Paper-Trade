import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.*;

/**
 * PaperTrade — Full Stock Market Simulator
 * Single-file Java Swing  •  JDK 17-25 compatible
 * Compile: javac PaperTrade.java
 * Run:     java PaperTrade
 */
public class PaperTrade {

    static final Color BG      = new Color(0x08090d);
    static final Color BG2     = new Color(0x0f1117);
    static final Color BG3     = new Color(0x161820);
    static final Color BG4     = new Color(0x1d1f2a);
    static final Color BORDER  = new Color(255,255,255,18);
    static final Color BORDER2 = new Color(255,255,255,30);
    static final Color GOLD    = new Color(0xc9a84c);
    static final Color GOLDBG  = new Color(201,168,76,20);
    static final Color GOLDBDR = new Color(201,168,76,64);
    static final Color TEXT    = new Color(0xf0f1f5);
    static final Color TEXT2   = new Color(0x9499b0);
    static final Color TEXT3   = new Color(0x585d78);
    static final Color GREEN   = new Color(0x22d37f);
    static final Color GREENBG = new Color(34,211,127,20);
    static final Color GREENBDR= new Color(34,211,127,51);
    static final Color RED     = new Color(0xf5455c);
    static final Color REDBG   = new Color(245,69,92,20);
    static final Color REDBDR  = new Color(245,69,92,51);
    static final Color BLUE    = new Color(0x4f8ef5);

    static final Font F_HEAD  = new Font("SansSerif",  Font.BOLD,   14);
    static final Font F_BODY  = new Font("SansSerif",  Font.PLAIN,  13);
    static final Font F_BODY2 = new Font("SansSerif",  Font.PLAIN,  12);
    static final Font F_MONO  = new Font("Monospaced", Font.PLAIN,  12);
    static final Font F_MONOB = new Font("Monospaced", Font.BOLD,   13);
    static final Font F_SMALL = new Font("Monospaced", Font.PLAIN,  11);
    static final Font F_TINY  = new Font("SansSerif",  Font.PLAIN,  10);
    static final Font F_TINYB = new Font("SansSerif",  Font.BOLD,   10);

    static class LiveStock {
        String sym, name, sector;
        double price, change, pct, open, high52, low52;
        long vol; double mktCap;
        final Map<String,List<Double>> hist = new LinkedHashMap<>();
        LiveStock(String sym,String name,String sector,double price,double change,double pct,
                  double open,double high52,double low52,long vol,double mktCap){
            this.sym=sym;this.name=name;this.sector=sector;
            this.price=price;this.change=change;this.pct=pct;
            this.open=open;this.high52=high52;this.low52=low52;
            this.vol=vol;this.mktCap=mktCap;
            for(String tf:new String[]{"1D","1W","1M","3M","1Y"}) hist.put(tf,genHist(price,tf));
        }
    }
    static class Holding { int shares; double cost; Holding(int s,double c){shares=s;cost=c;} }
    static class Order {
        String type,sym,time; int shares; double price,total;
        Order(String t,String s,int sh,double p,double tot,String tm){type=t;sym=s;shares=sh;price=p;total=tot;time=tm;}
    }

    static final Map<String,LiveStock> STOCKS   = new LinkedHashMap<>();
    static final Map<String,Holding>   holdings = new LinkedHashMap<>();
    static final List<Order>           orders   = new ArrayList<>();
    static final Random                RNG      = new Random();
    static final DecimalFormat         DF       = new DecimalFormat("#,##0.00");

    static double cash=10_000.0, loggedUser_unused=0;
    static String loggedUser="", currentSym="AAPL", currentTF="1D", activeScreen="dashboard";

    static JFrame     frame;
    static CardLayout rootLayout;
    static JPanel     rootPanel;

    static JTextField     tfUser, tfEmail, tfFirst, tfLast;
    static JPasswordField tfPass, tfConfirm;
    static JCheckBox      cbRemember;
    static JLabel         lblLoginErr;
    static CardLayout     loginCardLayout;
    static JPanel         loginCards;

    static JLabel  lblCash, lblNavInitials;
    static JButton btnNavDash, btnNavStocks, btnNavPay;

    static JPanel  toastPanel;
    static JLabel  toastIcon, toastTitle, toastMsg;
    static javax.swing.Timer toastTimer;

    static CardLayout screenLayout;
    static JPanel     screenPanel;

    static JLabel lblPortVal, lblPortPnl, lblDayPnl, lblBestStock;
    static JPanel dashActivityPanel, dashMarketPanel;

    static JPanel     stocksListPanel;
    static JTable     stockTable;
    static JLabel     lblDSym, lblDName, lblDPrice, lblDChg;
    static JLabel     lblDOpen, lblDHigh, lblDLow, lblDVol;
    static ChartView  stockChart;
    static JSpinner   buyQty, sellQty;
    static JLabel     lblBuyTotal, lblBuyAvail, lblSellTotal, lblSellHeld;
    static JTextField tfSearch;
    static JPanel     tfBtnRow;

    static JTextField tfCardNum, tfCardName, tfExpiry, tfCVV, tfCardAmt;
    static JTextField tfUpiId, tfUpiAmt;
    static JLabel     lblCNum, lblCName, lblCExp;
    static JLabel     lblSumSub, lblSumFee, lblSumBonus, lblSumTotal;
    static JPanel     payItems, txPanel;

    static boolean _upiVerified=false, _upiAppSelected=false;

    public static void main(String[] args){
        initData();
        SwingUtilities.invokeLater(()->{applyDarkDefaults();buildFrame();showLogin();startTick();});
    }

    static void initData(){
        addS("AAPL","Apple Inc.","Technology",        189.42, 2.14,1.14,187.28,198.50,164.08,58_200_000L,2.94e12);
        addS("MSFT","Microsoft Corp.","Technology",   415.23,-3.87,-0.92,419.10,430.82,362.50,22_100_000L,3.08e12);
        addS("GOOGL","Alphabet Inc.","Technology",    177.55, 1.23,0.70,176.32,193.31,130.67,19_800_000L,2.21e12);
        addS("AMZN","Amazon.com","Consumer",          198.64, 4.52,2.33,194.12,201.20,151.61,31_500_000L,2.07e12);
        addS("NVDA","NVIDIA Corp.","Technology",      875.40,22.18,2.60,853.22,974.00,463.76,45_700_000L,2.16e12);
        addS("META","Meta Platforms","Technology",    502.31,-8.42,-1.65,510.73,531.49,279.40,14_300_000L,1.27e12);
        addS("TSLA","Tesla Inc.","Automotive",        172.12,-5.63,-3.16,177.75,299.29,152.37,89_400_000L,5.50e11);
        addS("JPM","JPMorgan Chase","Finance",        198.85, 1.98,1.01,196.87,205.00,172.50,11_200_000L,5.74e11);
        addS("NFLX","Netflix Inc.","Media",           628.35, 8.43,1.36,619.92,700.00,542.01, 8_900_000L,2.70e11);
        addS("AMD","Advanced Micro","Technology",     177.22,-2.11,-1.18,179.33,227.30,155.84,16_400_000L,2.87e11);
    }
    static void addS(String sym,String name,String sector,double price,double chg,double pct,
                     double open,double hi,double lo,long vol,double mkt){
        STOCKS.put(sym,new LiveStock(sym,name,sector,price,chg,pct,open,hi,lo,vol,mkt));
    }
    static List<Double> genHist(double base,String tf){
        int pts; double v;
        switch(tf){case "1W"->{pts=7;v=0.018;}case "1M"->{pts=30;v=0.022;}
                   case "3M"->{pts=13;v=0.040;}case "1Y"->{pts=12;v=0.080;}
                   default->{pts=78;v=0.003;}}
        List<Double> d=new ArrayList<>(); double p=base*(0.95+RNG.nextDouble()*0.05);
        for(int i=0;i<pts;i++){p+=(RNG.nextDouble()-0.48)*v*p;d.add(Math.max(p,1));}
        d.add(base); return d;
    }

    static void applyDarkDefaults(){
        try{UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());}catch(Exception ignored){}
        Object[][] kv={
            {"Panel.background",BG},{"OptionPane.background",BG2},
            {"TextField.background",BG3},{"TextField.foreground",TEXT},{"TextField.caretForeground",TEXT},
            {"TextField.border",new LineBorder(BORDER2,1,true)},
            {"PasswordField.background",BG3},{"PasswordField.foreground",TEXT},{"PasswordField.caretForeground",TEXT},
            {"TextArea.background",BG3},{"TextArea.foreground",TEXT},{"Spinner.background",BG3},
            {"Button.background",BG3},{"Button.foreground",TEXT},{"Label.foreground",TEXT},
            {"CheckBox.background",BG2},{"CheckBox.foreground",TEXT2},
            // TabbedPane: all content areas must be opaque/solid
            {"TabbedPane.background",BG2},{"TabbedPane.foreground",TEXT2},{"TabbedPane.selected",BG3},
            {"TabbedPane.contentAreaColor",BG2},{"TabbedPane.tabAreaBackground",BG2},
            {"TabbedPane.shadow",BG2},{"TabbedPane.darkShadow",BG3},{"TabbedPane.light",BG2},
            {"Table.background",BG2},{"Table.foreground",TEXT},{"Table.gridColor",BORDER},
            {"Table.selectionBackground",BG4},{"Table.selectionForeground",TEXT},
            {"TableHeader.background",BG3},{"TableHeader.foreground",TEXT2},
            {"ScrollPane.background",BG2},{"Viewport.background",BG2},
            {"ScrollBar.background",BG2},{"ScrollBar.thumb",BG4},{"ScrollBar.track",BG2},
        };
        for(Object[] p:kv) UIManager.put((String)p[0],p[1]);
    }

    static void buildFrame(){
        frame=new JFrame("PaperTrade \u2014 Stock Market Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280,800); frame.setMinimumSize(new Dimension(1024,680));
        rootLayout=new CardLayout(); rootPanel=new JPanel(rootLayout); rootPanel.setBackground(BG);
        rootPanel.add(buildLoginPage(),"login"); rootPanel.add(buildAppShell(),"app");
        toastIcon=lbl("\u2713",new Font("SansSerif",Font.BOLD,16),GREEN);
        toastTitle=lbl("",new Font("SansSerif",Font.BOLD,13),TEXT);
        toastMsg=lbl("",F_BODY2,TEXT2);
        toastPanel=new JPanel(new BorderLayout(10,0)){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(22,24,32,245)); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(BORDER2); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14); g2.dispose();
            }
        };
        toastPanel.setOpaque(false); toastPanel.setBorder(new EmptyBorder(13,16,13,16));
        JPanel tc=new JPanel(new GridLayout(2,1,0,2)); tc.setOpaque(false); tc.add(toastTitle); tc.add(toastMsg);
        toastPanel.add(toastIcon,BorderLayout.WEST); toastPanel.add(tc,BorderLayout.CENTER);
        JPanel glass=new JPanel(null){
            @Override protected void paintComponent(Graphics g){}
            @Override public boolean contains(int x,int y){
                if(toastPanel!=null&&toastPanel.isVisible()){Rectangle tb=toastPanel.getBounds();if(tb.contains(x,y))return true;}
                return false;
            }
        };
        glass.setOpaque(false); glass.setFocusable(false); glass.add(toastPanel); toastPanel.setVisible(false);
        frame.setGlassPane(glass); glass.setVisible(false);
        glass.addComponentListener(new ComponentAdapter(){@Override public void componentResized(ComponentEvent e){positionToast();}});
        frame.setContentPane(rootPanel); frame.setVisible(true);
    }
    static void positionToast(){
        if(toastPanel==null||frame==null) return;
        JPanel glass=(JPanel)frame.getGlassPane();
        Dimension ps=new Dimension(340,64); toastPanel.setSize(ps);
        int x=glass.getWidth()-ps.width-24, y=glass.getHeight()-ps.height-24;
        y=Math.max(y,66); // never overlap nav bar
        toastPanel.setLocation(x,y);
    }
    static void showLogin(){
        cash=10_000.0;loggedUser="";holdings.clear();orders.clear();
        currentSym="AAPL";currentTF="1D";activeScreen="dashboard";rootLayout.show(rootPanel,"login");
    }
    static void showApp(){updateAllUI();rootLayout.show(rootPanel,"app");}

    // ═══ LOGIN ═══════════════════════════════════════════════════
    static JPanel buildLoginPage(){
        JPanel page=new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new RadialGradientPaint(new Point2D.Float(getWidth(),0),600,new float[]{0f,1f},new Color[]{new Color(201,168,76,18),new Color(0,0,0,0)}));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setPaint(new RadialGradientPaint(new Point2D.Float(0,getHeight()),500,new float[]{0f,1f},new Color[]{new Color(79,142,245,12),new Color(0,0,0,0)}));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        page.setBackground(BG); page.add(buildLoginLeft(),BorderLayout.CENTER); page.add(buildLoginRight(),BorderLayout.EAST); return page;
    }
    static JPanel buildLoginLeft(){
        JPanel p=new JPanel(){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(BORDER);for(int x=0;x<getWidth();x+=48)for(int y=0;y<getHeight();y+=48)g2.fillOval(x-1,y-1,2,2);g2.dispose();}};
        p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS)); p.setBackground(BG2);
        p.setBorder(new CompoundBorder(new MatteBorder(0,0,0,1,BORDER2),new EmptyBorder(60,60,60,60)));
        p.setPreferredSize(new Dimension(560,0));
        JLabel l1=lbl("Paper",new Font("SansSerif",Font.BOLD,44),TEXT); l1.setAlignmentX(0f);
        JLabel l2=lbl("Trade",new Font("SansSerif",Font.BOLD,44),GOLD); l2.setAlignmentX(0f);
        JPanel lr=hbox(0,l1,l2); lr.setAlignmentX(0f);
        JLabel tag=lbl("Professional Paper Trading Platform",F_BODY2,TEXT3); tag.setAlignmentX(0f);
        JLabel hero=new JLabel("<html><div style='font-size:26px;font-weight:bold'>Trade Smarter,<br>Risk <font color='#c9a84c'>Nothing</font></div></html>");
        hero.setForeground(TEXT); hero.setAlignmentX(0f);
        JLabel sub=new JLabel("<html><div style='width:380px;color:#585d78;font-size:12px;line-height:1.6'>Practice real market strategies with simulated capital.</div></html>"); sub.setAlignmentX(0f);
        JPanel stats=new JPanel(new GridLayout(1,3,16,0)); stats.setOpaque(false); stats.setAlignmentX(0f); stats.setMaximumSize(new Dimension(Integer.MAX_VALUE,90));
        stats.add(statCard("$10K","Starting Capital")); stats.add(statCard("10+","Live Stocks")); stats.add(statCard("0%","Commission"));
        p.add(lr); p.add(vs(4)); p.add(tag); p.add(vs(48)); p.add(hero); p.add(vs(14)); p.add(sub); p.add(vs(36)); p.add(stats); p.add(Box.createVerticalGlue()); return p;
    }
    static JPanel statCard(String v,String l){
        JPanel c=new JPanel(new GridLayout(2,1,0,4)); c.setBackground(BG3);
        c.setBorder(new CompoundBorder(new LineBorder(BORDER2,1,true),new EmptyBorder(16,18,16,18)));
        c.add(lbl(v,new Font("Monospaced",Font.BOLD,20),GOLD)); c.add(lbl(l,F_TINYB,TEXT3)); return c;
    }
    static JPanel buildLoginRight(){
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(BG);
        p.setPreferredSize(new Dimension(420,0)); p.setBorder(new EmptyBorder(60,44,60,44));
        loginCardLayout=new CardLayout(); loginCards=new JPanel(loginCardLayout); loginCards.setBackground(BG);
        loginCards.add(buildSigninForm(),"signin"); loginCards.add(buildSignupForm(),"signup"); loginCards.add(buildForgotForm(),"forgot");
        p.add(loginCards,BorderLayout.CENTER); return p;
    }
    static JPanel buildSigninForm(){
        JPanel outer=new JPanel(new BorderLayout()); outer.setBackground(BG); outer.add(loginTabRow(true),BorderLayout.NORTH);
        JPanel content=new JPanel(new GridBagLayout()); content.setBackground(BG); content.setBorder(new EmptyBorder(24,0,0,0));
        GridBagConstraints g=new GridBagConstraints(); g.fill=GridBagConstraints.HORIZONTAL; g.weightx=1.0; g.gridx=0;
        g.gridy=0;g.insets=new Insets(0,0,4,0);content.add(lbl("Welcome back",new Font("SansSerif",Font.BOLD,26),TEXT),g);
        g.gridy=1;g.insets=new Insets(0,0,24,0);content.add(lbl("Sign in to your trading account",F_BODY2,TEXT3),g);
        tfUser=field("Username or Email");
        g.gridy=2;g.insets=new Insets(0,0,6,0);content.add(flbl("Username / Email"),g);
        g.gridy=3;g.insets=new Insets(0,0,14,0);content.add(tfUser,g);
        tfPass=passField("Password");
        g.gridy=4;g.insets=new Insets(0,0,6,0);content.add(flbl("Password"),g);
        g.gridy=5;g.insets=new Insets(0,0,10,0);content.add(tfPass,g);
        cbRemember=new JCheckBox("Remember me"); cbRemember.setFont(F_BODY2); cbRemember.setBackground(BG); cbRemember.setForeground(TEXT2);
        JLabel forgotLink=link("Forgot password?"); forgotLink.addMouseListener(new MA(()->loginCardLayout.show(loginCards,"forgot")));
        JPanel remRow=new JPanel(new BorderLayout()); remRow.setOpaque(false); remRow.add(cbRemember,BorderLayout.WEST); remRow.add(forgotLink,BorderLayout.EAST);
        g.gridy=6;g.insets=new Insets(0,0,12,0);content.add(remRow,g);
        lblLoginErr=lbl(" ",F_BODY2,RED);
        g.gridy=7;g.insets=new Insets(0,0,8,0);content.add(lblLoginErr,g);
        JButton btn=goldBtn("Sign In"); btn.setPreferredSize(new Dimension(0,46)); btn.setBorder(new EmptyBorder(12,0,12,0)); btn.addActionListener(e->doLogin());
        g.gridy=8;g.insets=new Insets(0,0,0,0);content.add(btn,g);
        JPanel filler=new JPanel(); filler.setOpaque(false);
        g.gridy=9;g.weighty=1.0;g.fill=GridBagConstraints.BOTH;content.add(filler,g);
        outer.add(content,BorderLayout.CENTER); return outer;
    }
    static JPanel buildSignupForm(){
        JPanel outer=new JPanel(new BorderLayout()); outer.setBackground(BG); outer.add(loginTabRow(false),BorderLayout.NORTH);
        JPanel content=new JPanel(new GridBagLayout()); content.setBackground(BG); content.setBorder(new EmptyBorder(24,0,0,0));
        GridBagConstraints g=new GridBagConstraints(); g.fill=GridBagConstraints.HORIZONTAL; g.weightx=1.0; g.gridx=0;
        g.gridy=0;g.insets=new Insets(0,0,4,0);content.add(lbl("Create Account",new Font("SansSerif",Font.BOLD,26),TEXT),g);
        g.gridy=1;g.insets=new Insets(0,0,22,0);content.add(lbl("Start trading with $10,000 virtual cash",F_BODY2,TEXT3),g);
        tfFirst=field("First name"); tfLast=field("Last name");
        JPanel nameRow=new JPanel(new GridLayout(1,2,10,0)); nameRow.setOpaque(false); nameRow.add(tfFirst); nameRow.add(tfLast);
        g.gridy=2;g.insets=new Insets(0,0,6,0);content.add(flbl("Full Name"),g);
        g.gridy=3;g.insets=new Insets(0,0,14,0);content.add(nameRow,g);
        tfEmail=field("Email address");
        g.gridy=4;g.insets=new Insets(0,0,6,0);content.add(flbl("Email"),g);
        g.gridy=5;g.insets=new Insets(0,0,14,0);content.add(tfEmail,g);
        tfConfirm=passField("Password");
        g.gridy=6;g.insets=new Insets(0,0,6,0);content.add(flbl("Password"),g);
        g.gridy=7;g.insets=new Insets(0,0,20,0);content.add(tfConfirm,g);
        JButton btn=goldBtn("Create Account"); btn.setPreferredSize(new Dimension(0,46)); btn.setBorder(new EmptyBorder(12,0,12,0));
        btn.addActionListener(e->{String fn=tfFirst.getText().trim(),ln=tfLast.getText().trim(),em=tfEmail.getText().trim();
            if(fn.isEmpty()||ln.isEmpty()||!em.contains("@")){if(lblLoginErr!=null){lblLoginErr.setText("Fill all fields with a valid email.");lblLoginErr.setForeground(RED);}return;}
            loggedUser=fn+" "+ln; doEnterApp();});
        g.gridy=8;g.insets=new Insets(0,0,0,0);content.add(btn,g);
        JPanel filler=new JPanel(); filler.setOpaque(false);
        g.gridy=9;g.weighty=1.0;g.fill=GridBagConstraints.BOTH;content.add(filler,g);
        outer.add(content,BorderLayout.CENTER); return outer;
    }
    static JPanel buildForgotForm(){
        JPanel outer=new JPanel(new BorderLayout()); outer.setBackground(BG);
        JPanel content=new JPanel(new GridBagLayout()); content.setBackground(BG); content.setBorder(new EmptyBorder(32,0,0,0));
        GridBagConstraints g=new GridBagConstraints(); g.fill=GridBagConstraints.HORIZONTAL; g.weightx=1.0; g.gridx=0;
        JLabel back=link("\u2190 Back to Sign In"); back.addMouseListener(new MA(()->loginCardLayout.show(loginCards,"signin")));
        JTextField tfReset=field("Email address");
        g.gridy=0;g.insets=new Insets(0,0,4,0);content.add(back,g);
        g.gridy=1;g.insets=new Insets(24,0,4,0);content.add(lbl("Reset Password",new Font("SansSerif",Font.BOLD,26),TEXT),g);
        g.gridy=2;g.insets=new Insets(0,0,24,0);content.add(lbl("Enter your email to receive a reset link",F_BODY2,TEXT3),g);
        g.gridy=3;g.insets=new Insets(0,0,6,0);content.add(flbl("Email Address"),g);
        g.gridy=4;g.insets=new Insets(0,0,20,0);content.add(tfReset,g);
        JButton btn=goldBtn("Send Reset Link"); btn.setPreferredSize(new Dimension(0,46)); btn.setBorder(new EmptyBorder(12,0,12,0));
        btn.addActionListener(e->{String em=tfReset.getText().trim();if(!em.contains("@")){showMsg("Error","Enter a valid email.");return;}showMsg("Email Sent","Reset link sent to "+em);loginCardLayout.show(loginCards,"signin");});
        g.gridy=5;g.insets=new Insets(0,0,0,0);content.add(btn,g);
        JPanel filler=new JPanel(); filler.setOpaque(false);
        g.gridy=6;g.weighty=1.0;g.fill=GridBagConstraints.BOTH;content.add(filler,g);
        outer.add(content,BorderLayout.CENTER); return outer;
    }
    static JPanel loginTabRow(boolean signinActive){
        JPanel row=new JPanel(new GridLayout(1,2)); row.setBackground(BG2); row.setBorder(new LineBorder(BORDER2,1,true)); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        JButton tbIn=tabBtn("Sign In",signinActive); JButton tbOut=tabBtn("Register",!signinActive);
        tbIn.addActionListener(e->loginCardLayout.show(loginCards,"signin")); tbOut.addActionListener(e->loginCardLayout.show(loginCards,"signup"));
        row.add(tbIn); row.add(tbOut); return row;
    }
    static void doLogin(){
        String u=tfUser.getText().trim(); String pw=new String(tfPass.getPassword()).trim();
        if(u.isEmpty()||pw.isEmpty()){setErr("Please enter username and password.");return;}
        if(pw.length()<4){setErr("Invalid credentials. Try again.");return;}
        loggedUser=u.contains("@")?u.split("@")[0]:u;
        loggedUser=Character.toUpperCase(loggedUser.charAt(0))+loggedUser.substring(1); doEnterApp();
    }
    static void doEnterApp(){
        if(lblNavInitials!=null){String ini=loggedUser.length()>1?""+Character.toUpperCase(loggedUser.charAt(0))+Character.toUpperCase(loggedUser.charAt(loggedUser.length()-1)):loggedUser.toUpperCase();lblNavInitials.setText(ini);}
        showApp(); switchScreen("dashboard");
    }
    static void setErr(String msg){if(lblLoginErr!=null){lblLoginErr.setText(msg);lblLoginErr.setForeground(RED);}}

    // ═══ APP SHELL ═══════════════════════════════════════════════
    static JPanel buildAppShell(){
        JPanel shell=new JPanel(new GridBagLayout()); shell.setBackground(BG);
        GridBagConstraints gc=new GridBagConstraints(); gc.gridx=0; gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1.0;
        JPanel navHolder=buildTopNav(); navHolder.setPreferredSize(new Dimension(0,58)); navHolder.setMinimumSize(new Dimension(0,58)); navHolder.setMaximumSize(new Dimension(Integer.MAX_VALUE,58));
        gc.gridy=0; gc.weighty=0; shell.add(navHolder,gc);
        screenLayout=new CardLayout(); screenPanel=new JPanel(screenLayout); screenPanel.setBackground(BG);
        screenPanel.add(buildDashboard(),"dashboard"); screenPanel.add(buildStocksPage(),"stocks"); screenPanel.add(buildPaymentPage(),"payment");
        gc.gridy=1; gc.weighty=1.0; gc.fill=GridBagConstraints.BOTH; shell.add(screenPanel,gc); return shell;
    }
    static JPanel buildTopNav(){
        JPanel nav=new JPanel(new BorderLayout(0,0)){@Override protected void paintComponent(Graphics g){g.setColor(BG2);g.fillRect(0,0,getWidth(),getHeight());super.paintComponent(g);}};
        nav.setOpaque(true); nav.setBackground(BG2); nav.setBorder(new MatteBorder(0,0,1,0,BORDER2));
        nav.setPreferredSize(new Dimension(0,58)); nav.setMinimumSize(new Dimension(0,58)); nav.setMaximumSize(new Dimension(Integer.MAX_VALUE,58));
        JPanel left=new JPanel(); left.setLayout(new BoxLayout(left,BoxLayout.X_AXIS)); left.setOpaque(false); left.setBorder(new EmptyBorder(0,20,0,0));
        JLabel b1=lbl("Paper",new Font("SansSerif",Font.BOLD,18),TEXT); b1.setAlignmentY(0.5f);
        JLabel b2=lbl("Trade",new Font("SansSerif",Font.BOLD,18),GOLD); b2.setAlignmentY(0.5f);
        left.add(b1); left.add(b2); left.add(Box.createHorizontalStrut(28));
        btnNavDash=makeNavBtn("Dashboard"); btnNavStocks=makeNavBtn("Stocks"); btnNavPay=makeNavBtn("Payment");
        btnNavDash.addActionListener(e->switchScreen("dashboard")); btnNavStocks.addActionListener(e->switchScreen("stocks")); btnNavPay.addActionListener(e->switchScreen("payment"));
        left.add(btnNavDash); left.add(btnNavStocks); left.add(btnNavPay); nav.add(left,BorderLayout.WEST);
        JPanel right=new JPanel(); right.setLayout(new BoxLayout(right,BoxLayout.X_AXIS)); right.setOpaque(false); right.setBorder(new EmptyBorder(0,0,0,20));
        lblCash=lbl("$"+DF.format(cash),F_MONO,GOLD);
        JPanel cashPill=new JPanel(new FlowLayout(FlowLayout.CENTER,3,0)); cashPill.setBackground(GOLDBG); cashPill.setAlignmentY(0.5f); cashPill.setMaximumSize(new Dimension(200,28)); cashPill.setBorder(new CompoundBorder(new LineBorder(GOLDBDR,1,true),new EmptyBorder(4,10,4,10)));
        cashPill.add(lbl("Cash: ",F_SMALL,GOLD)); cashPill.add(lblCash);
        lblNavInitials=lbl("PT",new Font("SansSerif",Font.BOLD,12),Color.BLACK); lblNavInitials.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel ava=new JPanel(new BorderLayout()){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(GOLD);g2.fillOval(0,0,getWidth(),getHeight());g2.dispose();super.paintComponent(g);}};
        ava.setOpaque(false); ava.setPreferredSize(new Dimension(34,34)); ava.setMaximumSize(new Dimension(34,34)); ava.setMinimumSize(new Dimension(34,34)); ava.setAlignmentY(0.5f); ava.add(lblNavInitials,BorderLayout.CENTER);
        JButton btnLogout=new JButton("Logout"); btnLogout.setFont(F_BODY2); btnLogout.setForeground(TEXT2); btnLogout.setBackground(BG3); btnLogout.setOpaque(true); btnLogout.setBorder(new CompoundBorder(new LineBorder(BORDER2,1,true),new EmptyBorder(6,14,6,14))); btnLogout.setFocusPainted(false); btnLogout.setCursor(handCursor()); btnLogout.setAlignmentY(0.5f); btnLogout.addActionListener(e->showLogin());
        right.add(cashPill); right.add(Box.createHorizontalStrut(14)); right.add(ava); right.add(Box.createHorizontalStrut(14)); right.add(btnLogout); nav.add(right,BorderLayout.EAST); return nav;
    }
    static JButton makeNavBtn(String text){
        JButton b=new JButton(text){@Override protected void paintComponent(Graphics g){g.setColor(getBackground());g.fillRect(0,0,getWidth(),getHeight());if(getBackground().equals(GOLDBG)){g.setColor(GOLD);g.fillRect(0,getHeight()-2,getWidth(),2);}super.paintComponent(g);}};
        b.setFont(new Font("SansSerif",Font.PLAIN,13)); b.setForeground(TEXT2); b.setBackground(BG2);
        b.setOpaque(true); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(handCursor()); b.setAlignmentY(0.5f);
        b.setPreferredSize(new Dimension(110,58)); b.setMinimumSize(new Dimension(90,58)); b.setMaximumSize(new Dimension(130,58)); b.setBorder(new EmptyBorder(0,12,0,12));
        b.addMouseListener(new MouseAdapter(){public void mouseEntered(MouseEvent e){if(!b.getBackground().equals(GOLDBG))b.setBackground(BG3);}public void mouseExited(MouseEvent e){if(!b.getBackground().equals(GOLDBG))b.setBackground(BG2);}});
        return b;
    }
    static void switchScreen(String name){
        activeScreen=name; screenLayout.show(screenPanel,name); setNavActive(name);
        if(name.equals("dashboard")){updateKPIs();renderDashActivity();renderDashMarket();}
        if(name.equals("stocks")){refreshStocksList();refreshDetailPanel();updateTradeSummaries();}
        if(name.equals("payment")){updatePayOrders();renderTxHistory();}
    }
    static void setNavActive(String s){styleNav(btnNavDash,s.equals("dashboard"));styleNav(btnNavStocks,s.equals("stocks"));styleNav(btnNavPay,s.equals("payment"));}
    static void styleNav(JButton b,boolean active){if(b==null)return;b.setForeground(active?GOLD:TEXT2);b.setBackground(active?GOLDBG:BG2);}

    // ═══ DASHBOARD ═══════════════════════════════════════════════
    static JPanel buildDashboard(){
        JPanel body=new JPanel(); body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS)); body.setBackground(BG); body.setBorder(new EmptyBorder(28,32,32,32));
        int hour=LocalTime.now().getHour(); String tg=hour<12?"Good morning":hour<17?"Good afternoon":"Good evening";
        JLabel greet=lbl(tg+", "+(loggedUser.isEmpty()?"Trader":loggedUser),new Font("SansSerif",Font.BOLD,26),TEXT); greet.setAlignmentX(0f);
        JLabel greetSub=lbl("Here's your market overview for today.",F_BODY2,TEXT3); greetSub.setAlignmentX(0f);
        JPanel kpiRow=new JPanel(new GridLayout(1,4,16,0)); kpiRow.setOpaque(false); kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,110));
        lblPortVal=lbl("$10,000.00",new Font("Monospaced",Font.BOLD,20),TEXT); lblPortPnl=lbl("+$0.00",new Font("Monospaced",Font.BOLD,20),GREEN);
        lblDayPnl=lbl("$0.00",new Font("Monospaced",Font.BOLD,20),BLUE); lblBestStock=lbl("\u2014",new Font("Monospaced",Font.BOLD,20),TEXT);
        kpiRow.add(kpiCard("Portfolio Value",lblPortVal,"\u25c8",GOLD)); kpiRow.add(kpiCard("Total P&L",lblPortPnl,"\u2191",GREEN));
        kpiRow.add(kpiCard("Day's Gain/Loss",lblDayPnl,"\u2195",BLUE)); kpiRow.add(kpiCard("Best Performer",lblBestStock,"\u2605",GOLD));
        JPanel grid=new JPanel(new GridLayout(1,2,20,0)); grid.setOpaque(false); grid.setMaximumSize(new Dimension(Integer.MAX_VALUE,340));
        JPanel actCard=dashCard("Recent Activity"); dashActivityPanel=new JPanel(); dashActivityPanel.setLayout(new BoxLayout(dashActivityPanel,BoxLayout.Y_AXIS)); dashActivityPanel.setBackground(BG); dashActivityPanel.setOpaque(true); actCard.add(dashActivityPanel,BorderLayout.CENTER); renderDashActivity();
        JPanel mktCard=dashCard("Market Overview"); dashMarketPanel=new JPanel(); dashMarketPanel.setLayout(new BoxLayout(dashMarketPanel,BoxLayout.Y_AXIS)); dashMarketPanel.setBackground(BG2); dashMarketPanel.setOpaque(true); mktCard.add(darkScroll(dashMarketPanel),BorderLayout.CENTER); renderDashMarket();
        grid.add(actCard); grid.add(mktCard);
        JPanel secCard=dashCard("Sector Performance"); secCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,230)); secCard.add(buildSectorBars(),BorderLayout.CENTER);
        body.add(greet); body.add(vs(4)); body.add(greetSub); body.add(vs(24)); body.add(kpiRow); body.add(vs(20)); body.add(grid); body.add(vs(20)); body.add(secCard);
        JPanel wrap=new JPanel(new BorderLayout()); wrap.setBackground(BG); wrap.add(darkScroll(body)); return wrap;
    }
    static JPanel kpiCard(String l,JLabel v,String icon,Color accent){
        JPanel c=new JPanel(new BorderLayout(0,6)); c.setBackground(BG2); c.setBorder(new CompoundBorder(new LineBorder(BORDER2,1,true),new EmptyBorder(18,20,18,20)));
        JLabel li=lbl(icon,new Font("SansSerif",Font.PLAIN,18),accent); li.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel top=new JPanel(new BorderLayout()); top.setOpaque(false); top.add(lbl(l,F_TINYB,TEXT3),BorderLayout.WEST); top.add(li,BorderLayout.EAST);
        c.add(top,BorderLayout.NORTH); c.add(v,BorderLayout.CENTER); return c;
    }
    static JPanel dashCard(String title){
        JPanel c=new JPanel(new BorderLayout(0,12)); c.setBackground(BG2); c.setBorder(new CompoundBorder(new LineBorder(BORDER2,1,true),new EmptyBorder(20,22,20,22)));
        c.add(lbl(title,new Font("SansSerif",Font.BOLD,15),TEXT),BorderLayout.NORTH); return c;
    }
    static void renderDashActivity(){
        if(dashActivityPanel==null) return; dashActivityPanel.removeAll();
        List<Order> recent=new ArrayList<>(); int start=Math.max(0,orders.size()-5);
        for(int i=orders.size()-1;i>=start;i--) recent.add(orders.get(i));
        if(recent.isEmpty()){dashActivityPanel.add(lbl("No activity yet. Start trading!",F_BODY2,TEXT3));}
        else{for(Order o:recent){boolean isBuy=o.type.equals("buy"),isDep=o.type.equals("deposit");
            JPanel row=new JPanel(new BorderLayout(12,0)); row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,54)); row.setBorder(new MatteBorder(0,0,1,0,BORDER));
            JLabel ico=lbl(isDep?"\u2193":isBuy?"\u25b2":"\u25bc",new Font("SansSerif",Font.BOLD,14),isDep?BLUE:isBuy?GREEN:RED); ico.setBorder(new EmptyBorder(8,0,8,0));
            JLabel symL=lbl(o.sym,F_MONOB,TEXT); JLabel detL=lbl(isDep?"Deposit":o.shares+" sh @ $"+DF.format(o.price),F_BODY2,TEXT2);
            JPanel info=new JPanel(new GridLayout(2,1,0,2)); info.setOpaque(false); info.add(symL); info.add(detL);
            String sign=(isBuy?"-":"+"); JLabel amt=lbl(sign+"$"+DF.format(o.total),F_MONOB,isBuy?RED:GREEN); amt.setHorizontalAlignment(SwingConstants.RIGHT);
            JLabel tm=lbl(o.time,F_TINY,TEXT3); tm.setHorizontalAlignment(SwingConstants.RIGHT);
            JPanel amtCol=new JPanel(new GridLayout(2,1,0,2)); amtCol.setOpaque(false); amtCol.add(amt); amtCol.add(tm);
            row.add(ico,BorderLayout.WEST); row.add(info,BorderLayout.CENTER); row.add(amtCol,BorderLayout.EAST); dashActivityPanel.add(row);}}
        dashActivityPanel.revalidate(); dashActivityPanel.repaint();
    }
    static void renderDashMarket(){
        if(dashMarketPanel==null) return; dashMarketPanel.removeAll(); int count=0;
        for(LiveStock s:STOCKS.values()){if(count++>=8)break;
            JPanel row=new JPanel(new BorderLayout(10,0)); row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,48)); row.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,BORDER),new EmptyBorder(7,0,7,0)));
            JLabel sym=lbl(s.sym,F_MONOB,TEXT); sym.setPreferredSize(new Dimension(52,0));
            JLabel nm=lbl(s.name.length()>18?s.name.substring(0,18)+"\u2026":s.name,F_BODY2,TEXT2);
            JLabel price=lbl("$"+DF.format(s.price),F_MONO,TEXT); price.setHorizontalAlignment(SwingConstants.RIGHT);
            boolean up=s.change>=0; JLabel chg=lbl((up?"+":" ")+String.format("%.2f%%",s.pct),F_TINYB,up?GREEN:RED);
            chg.setOpaque(true); chg.setBackground(up?GREENBG:REDBG); chg.setBorder(new EmptyBorder(2,6,2,6)); chg.setHorizontalAlignment(SwingConstants.CENTER);
            JPanel rp=new JPanel(new BorderLayout(6,0)); rp.setOpaque(false); rp.add(price,BorderLayout.CENTER); rp.add(chg,BorderLayout.EAST);
            row.add(sym,BorderLayout.WEST); row.add(nm,BorderLayout.CENTER); row.add(rp,BorderLayout.EAST); dashMarketPanel.add(row);}
        dashMarketPanel.revalidate(); dashMarketPanel.repaint();
    }
    static JPanel buildSectorBars(){
        String[][] secs={{"Technology","82"},{"Finance","61"},{"Consumer","48"},{"Automotive","35"},{"Media","55"}};
        JPanel p=new JPanel(new GridLayout(secs.length,1,0,12)); p.setOpaque(false);
        for(String[] s:secs){int val=Integer.parseInt(s[1]);
            JPanel row=new JPanel(new BorderLayout(0,5)); row.setOpaque(false);
            JPanel top=new JPanel(new BorderLayout()); top.setOpaque(false); top.add(lbl(s[0],F_BODY2,TEXT2),BorderLayout.WEST); top.add(lbl(s[1]+"%",F_MONOB,GOLD),BorderLayout.EAST);
            JPanel bar=new JPanel(){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(BG4);g2.fillRoundRect(0,0,getWidth(),getHeight(),4,4);g2.setColor(GOLD);g2.fillRoundRect(0,0,(int)(getWidth()*val/100.0),getHeight(),4,4);g2.dispose();}};
            bar.setOpaque(false); bar.setPreferredSize(new Dimension(0,5));
            row.add(top,BorderLayout.NORTH); row.add(bar,BorderLayout.CENTER); p.add(row);}
        return p;
    }
    static void updateKPIs(){
        double portVal=cash; double bestPct=Double.NEGATIVE_INFINITY; String bestSym="\u2014";
        for(Map.Entry<String,Holding> e:holdings.entrySet()){LiveStock s=STOCKS.get(e.getKey());if(s==null)continue;portVal+=s.price*e.getValue().shares;if(s.pct>bestPct){bestPct=s.pct;bestSym=s.sym;}}
        double deposited=10000+orders.stream().filter(o->o.type.equals("deposit")).mapToDouble(o->o.total).sum();
        double pnl=portVal-deposited, dayPnl=holdings.entrySet().stream().mapToDouble(e->{LiveStock s=STOCKS.get(e.getKey());return s==null?0:s.change*e.getValue().shares;}).sum();
        if(lblPortVal!=null)lblPortVal.setText("$"+DF.format(portVal));
        if(lblPortPnl!=null){lblPortPnl.setText((pnl>=0?"+":"")+"$"+DF.format(pnl));lblPortPnl.setForeground(pnl>=0?GREEN:RED);}
        if(lblDayPnl!=null){lblDayPnl.setText((dayPnl>=0?"+":"")+"$"+DF.format(dayPnl));lblDayPnl.setForeground(dayPnl>=0?GREEN:RED);}
        if(lblBestStock!=null)lblBestStock.setText(bestSym); if(lblCash!=null)lblCash.setText("$"+DF.format(cash));
    }

    // ═══ STOCKS ══════════════════════════════════════════════════
    static JPanel buildStocksPage(){
        JPanel page=new JPanel(new BorderLayout(0,0)); page.setBackground(BG); page.setBorder(new EmptyBorder(20,28,20,28));
        // Search removed — clean title only
        JLabel topTitle=lbl("Markets",new Font("SansSerif",Font.BOLD,26),TEXT);
        topTitle.setBorder(new EmptyBorder(0,0,18,0));
        page.add(topTitle,BorderLayout.NORTH);
        tfSearch=field(""); // kept as stub so refreshStocksList() does not NPE
        JPanel body=new JPanel(new GridLayout(1,2,18,0)); body.setBackground(BG);
        String[] colNames={"Symbol / Name","Price","Change"};
        DefaultTableModel tableModel=new DefaultTableModel(colNames,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        stockTable=new JTable(tableModel); stockTable.setBackground(BG2); stockTable.setForeground(TEXT); stockTable.setFont(F_MONO); stockTable.setRowHeight(46);
        stockTable.setShowGrid(false); stockTable.setIntercellSpacing(new Dimension(0,0)); stockTable.setSelectionBackground(BG4); stockTable.setSelectionForeground(TEXT); stockTable.setFocusable(false); stockTable.setCursor(handCursor()); stockTable.setFillsViewportHeight(true); stockTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        stockTable.getColumnModel().getColumn(0).setMinWidth(100); stockTable.getColumnModel().getColumn(1).setMinWidth(100); stockTable.getColumnModel().getColumn(1).setPreferredWidth(115); stockTable.getColumnModel().getColumn(1).setMaxWidth(130); stockTable.getColumnModel().getColumn(2).setMinWidth(80); stockTable.getColumnModel().getColumn(2).setPreferredWidth(95); stockTable.getColumnModel().getColumn(2).setMaxWidth(110);
        JTableHeader th=stockTable.getTableHeader(); th.setBackground(BG3); th.setForeground(TEXT3); th.setFont(F_TINYB); th.setReorderingAllowed(false); th.setResizingAllowed(false); th.setBorder(new MatteBorder(0,0,1,0,BORDER2)); th.setPreferredSize(new Dimension(0,32));
        stockTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer(){{setOpaque(true);}@Override public Component getTableCellRendererComponent(JTable t,Object val,boolean sel,boolean foc,int r,int c){String sym=val!=null?val.toString():"";boolean active=sym.equals(currentSym);setBackground(active?BG4:(r%2==0?BG2:new Color(18,20,28)));setForeground(active?GOLD:TEXT);setFont(F_MONOB);setBorder(new EmptyBorder(0,14,0,8));setHorizontalAlignment(SwingConstants.LEFT);setText(sym);return this;}});
        stockTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer(){{setOpaque(true);}@Override public Component getTableCellRendererComponent(JTable t,Object val,boolean sel,boolean foc,int r,int c){String sym=(String)t.getValueAt(r,0);boolean active=sym.equals(currentSym);setBackground(active?BG4:(r%2==0?BG2:new Color(18,20,28)));setForeground(TEXT);setFont(F_MONO);setBorder(new EmptyBorder(0,4,0,12));setHorizontalAlignment(SwingConstants.RIGHT);setText(val!=null?val.toString():"");return this;}});
        stockTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer(){{setOpaque(true);}@Override public Component getTableCellRendererComponent(JTable t,Object val,boolean sel,boolean foc,int r,int c){String v=val!=null?val.toString():"";String sym=(String)t.getValueAt(r,0);boolean active=sym.equals(currentSym);boolean up=!v.startsWith("-");setBackground(active?BG4:(r%2==0?BG2:new Color(18,20,28)));setForeground(up?GREEN:RED);setFont(new Font("Monospaced",Font.BOLD,11));setBorder(new EmptyBorder(0,4,0,12));setHorizontalAlignment(SwingConstants.CENTER);setText(v);return this;}});
        DefaultTableCellRenderer hR0=new DefaultTableCellRenderer(); hR0.setBackground(BG3); hR0.setForeground(TEXT3); hR0.setFont(F_TINYB); hR0.setHorizontalAlignment(SwingConstants.LEFT); hR0.setBorder(new EmptyBorder(0,14,0,8)); hR0.setOpaque(true); stockTable.getColumnModel().getColumn(0).setHeaderRenderer(hR0);
        DefaultTableCellRenderer hR1=new DefaultTableCellRenderer(); hR1.setBackground(BG3); hR1.setForeground(TEXT3); hR1.setFont(F_TINYB); hR1.setHorizontalAlignment(SwingConstants.RIGHT); hR1.setBorder(new EmptyBorder(0,4,0,12)); hR1.setOpaque(true); stockTable.getColumnModel().getColumn(1).setHeaderRenderer(hR1);
        DefaultTableCellRenderer hR2=new DefaultTableCellRenderer(); hR2.setBackground(BG3); hR2.setForeground(TEXT3); hR2.setFont(F_TINYB); hR2.setHorizontalAlignment(SwingConstants.CENTER); hR2.setBorder(new EmptyBorder(0,4,0,12)); hR2.setOpaque(true); stockTable.getColumnModel().getColumn(2).setHeaderRenderer(hR2);
        stockTable.addMouseListener(new MA(()->{int row=stockTable.getSelectedRow();if(row>=0){currentSym=(String)stockTable.getValueAt(row,0);refreshStocksList();refreshDetailPanel();}}));
        stockTable.setUI(new BasicTableUI(){@Override public void paint(Graphics g,JComponent c){super.paint(g,c);g.setColor(BORDER);for(int i=0;i<stockTable.getRowCount();i++){int y=stockTable.getRowHeight()*i+stockTable.getRowHeight()-1;g.drawLine(0,y,c.getWidth(),y);}}});
        JScrollPane tableScroll=darkScroll(stockTable); tableScroll.setColumnHeaderView(th);
        JPanel listCard=new JPanel(new BorderLayout()); listCard.setBackground(BG2); listCard.setBorder(new LineBorder(BORDER2,1,true)); listCard.add(tableScroll,BorderLayout.CENTER);
        stocksListPanel=new JPanel(); stocksListPanel.setOpaque(false);
        body.add(listCard); body.add(buildStockDetail()); page.add(body,BorderLayout.CENTER); return page;
    }
    static JPanel buildStockDetail(){
        // Outer panel with scroll so nothing ever gets clipped
        JPanel p=new JPanel(new BorderLayout(0,0));
        p.setBackground(BG2); p.setBorder(new LineBorder(BORDER2,1,true));

        // ── NORTH: symbol header ──
        JPanel hdr=new JPanel(new BorderLayout(12,0)); hdr.setBackground(BG2);
        hdr.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,BORDER2),new EmptyBorder(16,22,12,22)));
        lblDSym=lbl("AAPL",new Font("SansSerif",Font.BOLD,28),TEXT);
        lblDName=lbl("Apple Inc.",F_BODY2,TEXT2);
        JPanel dcLeft=new JPanel(new GridLayout(2,1,0,3)); dcLeft.setOpaque(false);
        dcLeft.add(lblDSym); dcLeft.add(lblDName);
        lblDPrice=lbl("$189.42",new Font("Monospaced",Font.BOLD,26),TEXT);
        lblDPrice.setHorizontalAlignment(SwingConstants.RIGHT);
        lblDChg=lbl("+$2.14 (+1.14%)",F_MONO,GREEN);
        lblDChg.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel dcRight=new JPanel(new GridLayout(2,1,0,3)); dcRight.setOpaque(false);
        dcRight.add(lblDPrice); dcRight.add(lblDChg);
        hdr.add(dcLeft,BorderLayout.WEST); hdr.add(dcRight,BorderLayout.EAST);

        // ── Timeframe buttons ──
        tfBtnRow=new JPanel(new FlowLayout(FlowLayout.LEFT,5,6)); tfBtnRow.setBackground(BG2);
        tfBtnRow.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,BORDER2),new EmptyBorder(0,16,0,16)));
        for(String tf:new String[]{"1D","1W","1M","3M","1Y"}){
            JButton b=new JButton(tf); b.setFont(F_SMALL); b.setFocusPainted(false); b.setCursor(handCursor());
            boolean act=tf.equals(currentTF);
            b.setBackground(act?GOLDBG:BG3); b.setForeground(act?GOLD:TEXT2);
            b.setBorder(new CompoundBorder(new LineBorder(act?GOLDBDR:BORDER2,1,true),new EmptyBorder(4,10,4,10)));
            b.addActionListener(e->{currentTF=tf;refreshChart();refreshTfBtns();});
            tfBtnRow.add(b);}

        // ── Chart (tall) ──
        stockChart=new ChartView(); stockChart.setBackground(BG2);
        JPanel cw=new JPanel(new BorderLayout()); cw.setBackground(BG2);
        cw.setPreferredSize(new Dimension(0,240));
        cw.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,BORDER2),new EmptyBorder(8,16,8,16)));
        cw.add(stockChart,BorderLayout.CENTER);

        // ── 4 stat boxes — fixed height ──
        JPanel stats=new JPanel(new GridLayout(1,4,8,0)); stats.setBackground(BG2);
        stats.setPreferredSize(new Dimension(0,72));
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE,72));
        stats.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,BORDER2),new EmptyBorder(10,18,10,18)));
        lblDOpen=monoLbl("—"); lblDHigh=monoLbl("—");
        lblDLow=monoLbl("—");  lblDVol=monoLbl("—");
        stats.add(miniStat("Open",    lblDOpen));
        stats.add(miniStat("52W High",lblDHigh));
        stats.add(miniStat("52W Low", lblDLow));
        stats.add(miniStat("Volume",  lblDVol));

        // ── Trade row: BUY + SELL side by side, fixed height ──
        JPanel tradeRow=new JPanel(new GridLayout(1,2,10,0));
        tradeRow.setBackground(BG2);
        tradeRow.setBorder(new EmptyBorder(12,14,14,14));
        tradeRow.setPreferredSize(new Dimension(0,210));
        tradeRow.add(buildTradeSide(true));
        tradeRow.add(buildTradeSide(false));

        // Stack everything in a scrollable Y_AXIS panel
        JPanel inner=new JPanel(); inner.setLayout(new BoxLayout(inner,BoxLayout.Y_AXIS));
        inner.setBackground(BG2); inner.setOpaque(true);
        inner.add(hdr);
        inner.add(tfBtnRow);
        inner.add(cw);
        inner.add(stats);
        inner.add(tradeRow);

        JScrollPane sp=new JScrollPane(inner);
        sp.setBorder(null); sp.setOpaque(true); sp.setBackground(BG2);
        sp.getViewport().setOpaque(true); sp.getViewport().setBackground(BG2);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI(){
            @Override protected void configureScrollBarColors(){thumbColor=BG4;trackColor=BG2;}
            @Override protected JButton createDecreaseButton(int o){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
            @Override protected JButton createIncreaseButton(int o){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
        });
        p.add(sp,BorderLayout.CENTER);
        return p;
    }

    static JPanel buildTradeSide(boolean isBuy){
        // GridBagLayout — every row is a fixed pixel band, nothing overlaps
        JPanel p=new JPanel(new GridBagLayout());
        p.setBackground(BG3); p.setOpaque(true);
        p.setBorder(new CompoundBorder(
            new LineBorder(isBuy?GREENBDR:REDBDR,1,true),
            new EmptyBorder(12,12,12,12)));

        GridBagConstraints gc=new GridBagConstraints();
        gc.gridx=0; gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1.0;

        // Row 0 — BUY / SELL badge
        JLabel badge=lbl(isBuy?"  BUY  ":"  SELL  ",
            new Font("SansSerif",Font.BOLD,11), isBuy?GREEN:RED);
        badge.setOpaque(true); badge.setBackground(isBuy?GREENBG:REDBG);
        badge.setBorder(new EmptyBorder(3,6,3,6));
        gc.gridy=0; gc.insets=new Insets(0,0,8,0); gc.weighty=0;
        p.add(badge,gc);

        // Row 1 — "Quantity" label
        gc.gridy=1; gc.insets=new Insets(0,0,3,0);
        p.add(lbl("Quantity",F_TINYB,TEXT3),gc);

        // Row 2 — spinner
        JSpinner qty=new JSpinner(new SpinnerNumberModel(1,1,100000,1));
        styleSpinner(qty);
        qty.setPreferredSize(new Dimension(0,34));
        qty.setMinimumSize(new Dimension(0,34));
        if(isBuy) buyQty=qty; else sellQty=qty;
        qty.addChangeListener(e->updateTradeSummaries());
        gc.gridy=2; gc.insets=new Insets(0,0,8,0);
        p.add(qty,gc);

        // Row 3 — Total
        LiveStock s=STOCKS.get(currentSym);
        JLabel totLbl=lbl("Total: $"+DF.format(s.price),F_SMALL,TEXT2);
        if(isBuy) lblBuyTotal=totLbl; else lblSellTotal=totLbl;
        gc.gridy=3; gc.insets=new Insets(0,0,3,0);
        p.add(totLbl,gc);

        // Row 4 — Available / Holding
        JLabel infoLbl;
        if(isBuy){
            infoLbl=lbl("Available: $"+DF.format(cash),F_SMALL,TEXT3);
            lblBuyAvail=infoLbl;
        } else {
            infoLbl=lbl("Holding: 0 shares",F_SMALL,TEXT3);
            lblSellHeld=infoLbl;
        }
        gc.gridy=4; gc.insets=new Insets(0,0,10,0);
        p.add(infoLbl,gc);

        // Spacer row — pushes button to bottom
        JPanel spacer=new JPanel(); spacer.setOpaque(false);
        gc.gridy=5; gc.weighty=1.0; gc.fill=GridBagConstraints.BOTH;
        gc.insets=new Insets(0,0,0,0);
        p.add(spacer,gc);

        // Row 6 — Execute button (always at bottom, never overlaps)
        JButton exec=new JButton(isBuy?"Place Buy Order":"Place Sell Order");
        exec.setFont(new Font("SansSerif",Font.BOLD,13));
        exec.setBackground(isBuy?GREEN:RED);
        exec.setForeground(isBuy?Color.BLACK:Color.WHITE);
        exec.setOpaque(true); exec.setContentAreaFilled(true);
        exec.setBorderPainted(false); exec.setFocusPainted(false);
        exec.setCursor(handCursor());
        exec.setPreferredSize(new Dimension(0,44));
        exec.setMinimumSize(new Dimension(0,44));
        final boolean buy=isBuy;
        final Color base=isBuy?GREEN:RED;
        exec.addActionListener(e->executeTrade(buy?"buy":"sell",(Integer)qty.getValue()));
        exec.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){exec.setBackground(base.brighter());}
            public void mouseExited(MouseEvent e) {exec.setBackground(base);}
        });
        gc.gridy=6; gc.weighty=0; gc.fill=GridBagConstraints.HORIZONTAL;
        gc.insets=new Insets(0,0,0,0);
        p.add(exec,gc);
        return p;
    }
    static void refreshStocksList(){
        if(stockTable==null) return; String q=tfSearch!=null?tfSearch.getText().toLowerCase().trim():"";
        DefaultTableModel m=(DefaultTableModel)stockTable.getModel(); m.setRowCount(0);
        for(LiveStock s:STOCKS.values()){if(!q.isEmpty()&&!s.sym.toLowerCase().contains(q)&&!s.name.toLowerCase().contains(q))continue;m.addRow(new Object[]{s.sym,String.format("$%,9.2f",s.price),String.format("%+.2f%%",s.pct)});}
        for(int i=0;i<stockTable.getRowCount();i++){if(currentSym.equals(stockTable.getValueAt(i,0))){stockTable.setRowSelectionInterval(i,i);break;}} stockTable.repaint();
    }
    static void refreshDetailPanel(){
        LiveStock s=STOCKS.get(currentSym); if(s==null||lblDSym==null) return;
        lblDSym.setText(s.sym); lblDName.setText(s.name); lblDPrice.setText("$"+DF.format(s.price));
        boolean up=s.change>=0; lblDChg.setText((up?"+":"")+"$"+DF.format(s.change)+" ("+(up?"+":"")+String.format("%.2f%%",s.pct)+")"); lblDChg.setForeground(up?GREEN:RED);
        lblDOpen.setText("$"+DF.format(s.open)); lblDHigh.setText("$"+DF.format(s.high52)); lblDLow.setText("$"+DF.format(s.low52)); lblDVol.setText(fmtK(s.vol));
        refreshChart(); updateTradeSummaries();
    }
    static void refreshChart(){LiveStock s=STOCKS.get(currentSym);if(s==null||stockChart==null)return;stockChart.setData(s.hist.get(currentTF),s.change>=0);}
    static void refreshTfBtns(){if(tfBtnRow==null)return;for(Component c:tfBtnRow.getComponents()){if(c instanceof JButton b){boolean act=b.getText().equals(currentTF);b.setBackground(act?GOLDBG:BG3);b.setForeground(act?GOLD:TEXT2);b.setBorder(new CompoundBorder(new LineBorder(act?GOLDBDR:BORDER2,1,true),new EmptyBorder(4,10,4,10)));}}}
    static void updateTradeSummaries(){
        LiveStock s=STOCKS.get(currentSym); if(s==null) return;
        if(buyQty!=null&&lblBuyTotal!=null)lblBuyTotal.setText("Total: $"+DF.format((Integer)buyQty.getValue()*s.price));
        if(buyQty!=null&&lblBuyAvail!=null)lblBuyAvail.setText("Avail: $"+DF.format(cash));
        if(sellQty!=null&&lblSellTotal!=null)lblSellTotal.setText("Total: $"+DF.format((Integer)sellQty.getValue()*s.price));
        if(sellQty!=null&&lblSellHeld!=null){Holding h=holdings.get(currentSym);lblSellHeld.setText("Holding: "+(h!=null?h.shares:0)+" shares");}
    }
    static void executeTrade(String type,int qty){
        LiveStock s=STOCKS.get(currentSym); double price=s.price,total=qty*price;
        String time=LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        if(type.equals("buy")){
            if(total>cash){showMsg("Insufficient Cash","Need $"+DF.format(total)+", have $"+DF.format(cash));return;}
            cash-=total; Holding ex=holdings.get(currentSym); if(ex!=null){ex.shares+=qty;ex.cost+=total;}else holdings.put(currentSym,new Holding(qty,total));
            orders.add(new Order("buy",currentSym,qty,price,total,time)); showMsg("Buy Order Placed","Bought "+qty+" shares of "+currentSym+" @ $"+DF.format(price));
        }else{
            Holding h=holdings.get(currentSym); if(h==null||h.shares<qty){showMsg("Insufficient Shares","You only have "+(h!=null?h.shares:0)+" shares.");return;}
            cash+=total; h.cost-=(h.cost/h.shares)*qty; h.shares-=qty; if(h.shares<=0)holdings.remove(currentSym);
            orders.add(new Order("sell",currentSym,qty,price,total,time)); showMsg("Sell Order Placed","Sold "+qty+" shares of "+currentSym+" @ $"+DF.format(price));
        }
        updateAllUI(); refreshStocksList(); updateTradeSummaries();
    }

    // ═══ PAYMENT ═════════════════════════════════════════════════
    // JTabbedPane removed — it always bleeds on CrossPlatformLAF
    // regardless of opacity flags. Hand-built tab system with pure
    // JPanel + CardLayout: every pixel is under our control.
    static JPanel buildPaymentPage(){
        JPanel page=new JPanel(new BorderLayout(16,0));
        page.setBackground(BG); page.setOpaque(true);
        page.setBorder(new EmptyBorder(20,28,20,28));
        JLabel title=lbl("Payment Gateway",new Font("SansSerif",Font.BOLD,26),TEXT);
        title.setBorder(new EmptyBorder(0,0,14,0));
        page.add(title, BorderLayout.NORTH);

        // Hand-built tab container — no JTabbedPane
        JPanel formArea=new JPanel(new BorderLayout(0,0));
        formArea.setBackground(BG2); formArea.setOpaque(true);
        formArea.setBorder(new LineBorder(BORDER2,1,true));

        // Tab bar: two fully opaque solid buttons
        JPanel tabBar=new JPanel(new GridLayout(1,2,0,0));
        tabBar.setBackground(BG3); tabBar.setOpaque(true);
        tabBar.setBorder(new MatteBorder(0,0,1,0,BORDER2));
        tabBar.setPreferredSize(new Dimension(0,38));
        JButton tabCard=new JButton("   Credit / Debit Card");
        JButton tabUpi =new JButton("   UPI Transfer");
        for(JButton b:new JButton[]{tabCard,tabUpi}){
            b.setFont(F_BODY2); b.setOpaque(true); b.setContentAreaFilled(true);
            b.setBorderPainted(true); b.setFocusPainted(false); b.setCursor(handCursor());
        }

        // Content — CardLayout, both panels solid opaque
        CardLayout cl=new CardLayout();
        JPanel content=new JPanel(cl);
        content.setBackground(BG2); content.setOpaque(true);
        JPanel cardContent=new JPanel(new BorderLayout());
        cardContent.setBackground(BG2); cardContent.setOpaque(true);
        cardContent.add(buildCardPanel(), BorderLayout.CENTER);
        JPanel upiContent=new JPanel(new BorderLayout());
        upiContent.setBackground(BG2); upiContent.setOpaque(true);
        upiContent.add(buildUPIPanel(), BorderLayout.CENTER);
        content.add(cardContent,"card");
        content.add(upiContent, "upi");

        // ONE scroll pane around the entire content area — no nested scroll panes
        JScrollPane contentScroll=new JScrollPane(content);
        contentScroll.setBorder(null);
        contentScroll.setOpaque(true); contentScroll.setBackground(BG2);
        contentScroll.getViewport().setOpaque(true); contentScroll.getViewport().setBackground(BG2);
        contentScroll.getVerticalScrollBar().setUnitIncrement(12);
        contentScroll.getVerticalScrollBar().setUI(new BasicScrollBarUI(){
            @Override protected void configureScrollBarColors(){thumbColor=BG4;trackColor=BG2;}
            @Override protected JButton createDecreaseButton(int o){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
            @Override protected JButton createIncreaseButton(int o){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
        });

        Runnable showCard=()->{
            cl.show(content,"card");
            tabCard.setBackground(BG2); tabCard.setForeground(GOLD);
            tabCard.setBorder(new MatteBorder(0,0,2,0,GOLD));
            tabUpi.setBackground(BG3);  tabUpi.setForeground(TEXT2);
            tabUpi.setBorder(new MatteBorder(0,0,0,0,BG3));
        };
        Runnable showUpi=()->{
            cl.show(content,"upi");
            tabUpi.setBackground(BG2);  tabUpi.setForeground(GOLD);
            tabUpi.setBorder(new MatteBorder(0,0,2,0,GOLD));
            tabCard.setBackground(BG3); tabCard.setForeground(TEXT2);
            tabCard.setBorder(new MatteBorder(0,0,0,0,BG3));
        };
        tabCard.addActionListener(e->showCard.run());
        tabUpi.addActionListener(e->showUpi.run());
        showCard.run();

        tabBar.add(tabCard); tabBar.add(tabUpi);
        formArea.add(tabBar,         BorderLayout.NORTH);
        formArea.add(contentScroll,  BorderLayout.CENTER);
        page.add(formArea, BorderLayout.CENTER);

        // EAST: sidebar — different slot, structurally cannot overlap CENTER
        JPanel sidebar=new JPanel(new BorderLayout(0,14));
        sidebar.setOpaque(true); sidebar.setBackground(BG);
        sidebar.setPreferredSize(new Dimension(330,0));
        sidebar.add(buildOrderSummaryCard(), BorderLayout.NORTH);
        sidebar.add(buildTxHistoryCard(),    BorderLayout.CENTER);
        page.add(sidebar, BorderLayout.EAST);
        return page;
    }

    static JPanel buildCardPanel(){
        JPanel p=new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS)); p.setBackground(BG2); p.setOpaque(true); p.setBorder(new EmptyBorder(20,22,20,22));
        JPanel preview=new JPanel(new BorderLayout(0,8)){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setPaint(new GradientPaint(0,0,new Color(0x1a1a2e),getWidth(),getHeight(),new Color(0x16213e)));g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);g2.setColor(new Color(255,255,255,12));g2.setStroke(new BasicStroke(1));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);g2.dispose();super.paintComponent(g);}};
        preview.setOpaque(true); preview.setAlignmentX(0f); preview.setMaximumSize(new Dimension(Integer.MAX_VALUE,155)); preview.setPreferredSize(new Dimension(0,155)); preview.setBorder(new EmptyBorder(18,22,18,22));
        lblCNum=lbl("\u2022\u2022\u2022\u2022 \u2022\u2022\u2022\u2022 \u2022\u2022\u2022\u2022 \u2022\u2022\u2022\u2022",new Font("Monospaced",Font.BOLD,16),TEXT);
        lblCName=lbl("CARD HOLDER",F_MONOB,TEXT2); lblCExp=lbl("MM/YY",F_MONO,TEXT2);
        JPanel cBot=new JPanel(new BorderLayout()); cBot.setOpaque(false); cBot.add(lblCName,BorderLayout.WEST); cBot.add(lblCExp,BorderLayout.EAST);
        preview.add(lbl("\u2b1b  CHIP",F_BODY2,GOLD),BorderLayout.NORTH); preview.add(lblCNum,BorderLayout.CENTER); preview.add(cBot,BorderLayout.SOUTH);
        tfCardNum=field("1234 5678 9012 3456"); tfCardNum.setAlignmentX(0f); setMaxH(tfCardNum,40);
        tfCardName=field("Cardholder Name"); tfCardName.setAlignmentX(0f); setMaxH(tfCardName,40);
        tfExpiry=field("MM/YY"); tfExpiry.setAlignmentX(0f); setMaxH(tfExpiry,40);
        tfCVV=field("CVV"); tfCVV.setAlignmentX(0f); setMaxH(tfCVV,40);
        tfCardAmt=field("0.00"); tfCardAmt.setAlignmentX(0f); setMaxH(tfCardAmt,40);
        tfCardNum.getDocument().addDocumentListener(new DocL(()->{String raw=tfCardNum.getText().replaceAll("\\D","");lblCNum.setText(raw.isEmpty()?"\u2022\u2022\u2022\u2022 \u2022\u2022\u2022\u2022 \u2022\u2022\u2022\u2022 \u2022\u2022\u2022\u2022":fmtCardNum(raw));}));
        tfCardName.getDocument().addDocumentListener(new DocL(()->{String n=tfCardName.getText().trim();lblCName.setText(n.isEmpty()?"CARD HOLDER":n.toUpperCase());}));
        tfExpiry.getDocument().addDocumentListener(new DocL(()->{String v=tfExpiry.getText().trim();lblCExp.setText(v.isEmpty()?"MM/YY":v);}));
        JPanel exRow=new JPanel(new GridLayout(1,2,10,0)); exRow.setOpaque(false); exRow.setAlignmentX(0f); exRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,40)); exRow.add(tfExpiry); exRow.add(tfCVV);
        JLabel cardErr=lbl(" ",F_BODY2,RED); cardErr.setAlignmentX(0f);
        JButton pay=goldBtn("Add Funds to Account"); pay.setAlignmentX(0f); setMaxH(pay,46);
        pay.addActionListener(e->{
            String rawNum=tfCardNum.getText().replaceAll("\\D",""), name=tfCardName.getText().trim(), expiry=tfExpiry.getText().trim(), cvv=tfCVV.getText().trim(), amtTxt=tfCardAmt.getText().trim();
            List<String> errs=new ArrayList<>();
            if(rawNum.length()<16) errs.add("\u2022 Card number must be 16 digits.");
            if(name.isEmpty())     errs.add("\u2022 Cardholder name is required.");
            if(!expiry.matches("\\d{2}/\\d{2}")) errs.add("\u2022 Expiry must be MM/YY format.");
            if(cvv.length()<3)     errs.add("\u2022 CVV must be 3 or 4 digits.");
            double amt=0; try{amt=Double.parseDouble(amtTxt.replaceAll("[^0-9.]",""));}catch(Exception ex){amt=0;}
            if(amt<=0) errs.add("\u2022 Amount must be greater than $0.");
            if(!errs.isEmpty()){cardErr.setText("<html>"+String.join("<br>",errs)+"</html>");showToast("Validation Error","Please fix card details.",false);return;}
            cardErr.setText(" "); processPayment(String.valueOf(amt));
        });
        p.add(preview); p.add(vs(18)); p.add(flbl("Card Number")); p.add(vs(6)); p.add(tfCardNum); p.add(vs(12));
        p.add(flbl("Cardholder Name")); p.add(vs(6)); p.add(tfCardName); p.add(vs(12));
        p.add(flbl("Expiry / CVV")); p.add(vs(6)); p.add(exRow); p.add(vs(12));
        p.add(flbl("Amount (USD)")); p.add(vs(6)); p.add(tfCardAmt); p.add(vs(10));
        p.add(cardErr); p.add(vs(8)); p.add(pay); p.add(Box.createVerticalGlue());
        // Return plain JPanel — scrolling handled by outer wrapper
        return p;
    }

    /** UPI panel — GridBagLayout guarantees zero overlap.
     *  Viewport is explicitly opaque to block sidebar ghost-painting. */
    static JPanel buildUPIPanel(){
        _upiVerified=false; _upiAppSelected=false;
        JPanel root=new JPanel(new GridBagLayout()); root.setBackground(BG2); root.setOpaque(true); root.setBorder(new EmptyBorder(18,20,18,20));
        GridBagConstraints gc=new GridBagConstraints(); gc.gridx=0; gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1.0;

        gc.gridy=0; gc.insets=new Insets(0,0,10,0); root.add(lbl("Choose UPI App",F_BODY2,TEXT2),gc);

        JPanel apps=new JPanel(new GridLayout(1,4,10,0)); apps.setBackground(BG2); apps.setOpaque(true); apps.setPreferredSize(new Dimension(0,70));
        ButtonGroup upiGroup=new ButtonGroup();
        for(String[] app:new String[][]{{"G Pay","G"},{"PhonePe","P"},{"Paytm","\u20b9"},{"BHIM","B"}}){
            JToggleButton tb=new JToggleButton("<html><center><b>"+app[1]+"</b><br><small>"+app[0]+"</small></center></html>");
            tb.setFont(F_BODY2); tb.setForeground(TEXT2); tb.setBackground(BG3); tb.setOpaque(true); tb.setFocusPainted(false); tb.setCursor(handCursor());
            tb.setBorder(new CompoundBorder(new LineBorder(BORDER2,1,true),new EmptyBorder(6,4,6,4)));
            tb.addItemListener(ev->{boolean sel=(ev.getStateChange()==java.awt.event.ItemEvent.SELECTED);
                tb.setBackground(sel?GOLDBG:BG3); tb.setForeground(sel?GOLD:TEXT2);
                tb.setBorder(new CompoundBorder(new LineBorder(sel?GOLDBDR:BORDER2,1,true),new EmptyBorder(6,4,6,4)));
                if(sel) _upiAppSelected=true;});
            upiGroup.add(tb); apps.add(tb);}
        gc.gridy=1; gc.insets=new Insets(0,0,16,0); root.add(apps,gc);

        gc.gridy=2; gc.insets=new Insets(0,0,6,0); root.add(flbl("UPI ID"),gc);
        tfUpiId=field("yourname@bank"); tfUpiId.setPreferredSize(new Dimension(0,40)); tfUpiId.getDocument().addDocumentListener(new DocL(()->_upiVerified=false));
        gc.gridy=3; gc.insets=new Insets(0,0,10,0); root.add(tfUpiId,gc);

        // Verify UPI ID — identical structure and style to "Transfer via UPI"
        // goldBtn = gold background, bold font, full-width, opaque, no border painting
        JButton verify=goldBtn("Verify UPI ID");
        verify.setPreferredSize(new Dimension(0,48));
        gc.gridy=4; gc.insets=new Insets(0,0,10,0); root.add(verify,gc);

        // Status label row — reserved height prevents layout shift when text changes
        JLabel verLbl=new JLabel(" ");
        verLbl.setFont(F_BODY2); verLbl.setForeground(GREEN);
        verLbl.setPreferredSize(new Dimension(0,22));
        gc.gridy=5; gc.insets=new Insets(0,0,16,0); root.add(verLbl,gc);

        verify.addActionListener(e->{String id=tfUpiId.getText().trim();
            if(id.matches(".+@[a-zA-Z]+")){_upiVerified=true;verLbl.setText("✓  UPI ID Verified");verLbl.setForeground(GREEN);showToast("Verified","UPI ID verified.",true);}
            else{_upiVerified=false;verLbl.setText("✗  Invalid UPI ID");verLbl.setForeground(RED);showToast("Error","Enter a valid UPI ID (e.g. name@bank)",false);}});
        gc.gridy=6; gc.insets=new Insets(0,0,6,0); root.add(flbl("Amount (USD)"),gc);
        tfUpiAmt=field("0.00"); tfUpiAmt.setPreferredSize(new Dimension(0,40));
        gc.gridy=7; gc.insets=new Insets(0,0,8,0); root.add(tfUpiAmt,gc);

        JLabel upiErr=new JLabel(" "); upiErr.setFont(F_BODY2); upiErr.setForeground(RED); upiErr.setPreferredSize(new Dimension(0,22));
        gc.gridy=8; gc.insets=new Insets(0,0,12,0); root.add(upiErr,gc);

        JButton pay=goldBtn("Transfer via UPI"); pay.setPreferredSize(new Dimension(0,48));
        pay.addActionListener(e->{
            List<String> errs=new ArrayList<>();
            if(!_upiAppSelected) errs.add("\u2022 Please select a UPI app.");
            if(tfUpiId.getText().trim().isEmpty()) errs.add("\u2022 UPI ID cannot be empty.");
            if(!_upiVerified) errs.add("\u2022 UPI ID must be verified before transfer.");
            double amt=0; try{amt=Double.parseDouble(tfUpiAmt.getText().trim().replaceAll("[^0-9.]",""));}catch(Exception ex){amt=0;}
            if(amt<=0) errs.add("\u2022 Amount must be greater than $0.");
            if(!errs.isEmpty()){upiErr.setText("<html>"+String.join("<br>",errs)+"</html>");showToast("Validation Error","Fix errors before transfer.",false);return;}
            upiErr.setText(" "); processPayment(String.valueOf(amt));});
        gc.gridy=9; gc.insets=new Insets(0,0,20,0); root.add(pay,gc);

        JPanel qrRow=new JPanel(new FlowLayout(FlowLayout.CENTER,0,0)); qrRow.setBackground(BG2); qrRow.setOpaque(true);
        QRPanel qr=new QRPanel(); qr.setPreferredSize(new Dimension(130,130)); qr.setMinimumSize(new Dimension(130,130)); qr.setMaximumSize(new Dimension(130,130)); qrRow.add(qr);
        gc.gridy=10; gc.insets=new Insets(0,0,0,0); root.add(qrRow,gc);

        JPanel filler=new JPanel(); filler.setBackground(BG2); filler.setOpaque(true);
        gc.gridy=11; gc.weighty=1.0; gc.fill=GridBagConstraints.BOTH; root.add(filler,gc);
        // Return plain JPanel — no JScrollPane, no transparent viewport
        return root;
    }

    static JPanel buildOrderSummaryCard(){
        JPanel c=new JPanel(new BorderLayout(0,10)); c.setBackground(BG2); c.setOpaque(true); c.setBorder(new CompoundBorder(new LineBorder(BORDER2,1,true),new EmptyBorder(18,18,18,18)));
        payItems=new JPanel(); payItems.setLayout(new BoxLayout(payItems,BoxLayout.Y_AXIS)); payItems.setBackground(BG2); payItems.setOpaque(true);
        JPanel totals=new JPanel(new GridLayout(4,2,4,6)); totals.setOpaque(false); totals.setBorder(new CompoundBorder(new MatteBorder(1,0,0,0,BORDER2),new EmptyBorder(12,0,0,0)));
        lblSumSub=monoLbl("$0.00"); lblSumFee=monoLbl("$0.00"); lblSumBonus=monoLbl("+$0.00"); lblSumTotal=monoLbl("$0.00"); lblSumTotal.setForeground(GOLD);
        totals.add(lbl("Subtotal",F_BODY2,TEXT2)); totals.add(rAlign(lblSumSub)); totals.add(lbl("Fee (0.1%)",F_BODY2,TEXT2)); totals.add(rAlign(lblSumFee));
        totals.add(lbl("Cashback",F_BODY2,TEXT2)); totals.add(rAlign(lblSumBonus)); totals.add(lbl("Total",new Font("SansSerif",Font.BOLD,13),TEXT)); totals.add(rAlign(lblSumTotal));
        c.add(lbl("Order Summary",new Font("SansSerif",Font.BOLD,14),TEXT),BorderLayout.NORTH); c.add(payItems,BorderLayout.CENTER); c.add(totals,BorderLayout.SOUTH); return c;
    }
    static JPanel buildTxHistoryCard(){
        JPanel c=new JPanel(new BorderLayout(0,10)); c.setBackground(BG2); c.setOpaque(true); c.setBorder(new CompoundBorder(new LineBorder(BORDER2,1,true),new EmptyBorder(18,18,18,18)));
        txPanel=new JPanel(); txPanel.setLayout(new BoxLayout(txPanel,BoxLayout.Y_AXIS)); txPanel.setBackground(BG2); txPanel.setOpaque(true);
        JScrollPane sp=darkScroll(txPanel); sp.setPreferredSize(new Dimension(0,200));
        c.add(lbl("Transaction History",new Font("SansSerif",Font.BOLD,14),TEXT),BorderLayout.NORTH); c.add(sp,BorderLayout.CENTER); return c;
    }
    static void updatePayOrders(){
        if(payItems==null) return; payItems.removeAll();
        List<Order> trades=orders.stream().filter(o->!o.type.equals("deposit")).collect(Collectors.toList());
        if(trades.isEmpty()){payItems.add(lbl("No orders yet.",F_BODY2,TEXT3));}
        else{int start=Math.max(0,trades.size()-4);for(int i=trades.size()-1;i>=start;i--){Order o=trades.get(i);boolean buy=o.type.equals("buy");
            JPanel row=new JPanel(new BorderLayout(10,0)); row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,46)); row.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,BORDER),new EmptyBorder(6,0,6,0)));
            JLabel badge=lbl(o.type.toUpperCase(),F_TINYB,buy?GREEN:RED); badge.setOpaque(true); badge.setBackground(buy?GREENBG:REDBG); badge.setBorder(new EmptyBorder(2,6,2,6));
            JLabel sym=lbl(o.sym,F_MONOB,TEXT); JLabel sub=lbl(o.shares+" sh @ $"+DF.format(o.price),F_TINY,TEXT2);
            JPanel info=new JPanel(new GridLayout(2,1,0,2)); info.setOpaque(false); info.add(sym); info.add(sub);
            JLabel val=lbl("$"+DF.format(o.total),F_MONOB,TEXT); val.setHorizontalAlignment(SwingConstants.RIGHT);
            row.add(badge,BorderLayout.WEST); row.add(info,BorderLayout.CENTER); row.add(val,BorderLayout.EAST); payItems.add(row);}}
        double sub=trades.stream().mapToDouble(o->o.total).sum(); double fee=sub*.001,bonus=sub>500?sub*.005:0;
        if(lblSumSub!=null)lblSumSub.setText("$"+DF.format(sub)); if(lblSumFee!=null)lblSumFee.setText("$"+DF.format(fee));
        if(lblSumBonus!=null)lblSumBonus.setText("+$"+DF.format(bonus)); if(lblSumTotal!=null)lblSumTotal.setText("$"+DF.format(sub+fee-bonus));
        payItems.revalidate(); payItems.repaint();
    }
    static void renderTxHistory(){
        if(txPanel==null) return; txPanel.removeAll();
        if(orders.stream().noneMatch(o->o.type.equals("deposit"))) addTxRow("\u2193","Deposit","Bank Transfer","Today 09:15","+$5,000",true,true);
        for(int i=orders.size()-1;i>=0&&txPanel.getComponentCount()<7;i--){Order o=orders.get(i);boolean buy=o.type.equals("buy"),dep=o.type.equals("deposit");
            addTxRow(dep?"\u2193":buy?"\u25b2":"\u25bc",dep?"Deposit":o.sym,dep?"Card Payment":o.shares+" shares "+(buy?"bought":"sold"),o.time,(dep||!buy?"+":"-")+"$"+DF.format(o.total),dep||!buy,true);}
        txPanel.revalidate(); txPanel.repaint();
    }
    static void addTxRow(String icon,String lb,String sub,String time,String amount,boolean up,boolean done){
        JPanel row=new JPanel(new BorderLayout(10,0)); row.setBackground(BG2); row.setOpaque(true); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,52));
        row.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,BORDER),new EmptyBorder(8,0,8,0)));
        JLabel ico=lbl(icon,F_BODY2,icon.equals("\u2193")?BLUE:icon.equals("\u25b2")?GREEN:RED); ico.setBorder(new EmptyBorder(0,4,0,4));
        JLabel ll=lbl(lb,F_MONOB,TEXT); JLabel sl=lbl(sub+" \u00b7 "+time,F_TINY,TEXT2);
        JPanel info=new JPanel(new GridLayout(2,1,0,2)); info.setOpaque(false); info.add(ll); info.add(sl);
        JLabel stl=lbl(done?"Done":"Pending",F_TINY,done?GREEN:GOLD);
        JLabel aml=lbl(amount,F_MONOB,up?GREEN:RED); aml.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel ac=new JPanel(new GridLayout(2,1,0,2)); ac.setOpaque(false); ac.add(aml); ac.add(stl);
        row.add(ico,BorderLayout.WEST); row.add(info,BorderLayout.CENTER); row.add(ac,BorderLayout.EAST); txPanel.add(row);
    }
    static void processPayment(String amtStr){
        double amt; try{amt=Double.parseDouble(amtStr.replaceAll("[^0-9.]",""));}catch(Exception e){amt=0;}
        if(amt<=0){showMsg("Invalid Amount","Enter a valid deposit amount.");return;}
        cash+=amt; String time=LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        orders.add(new Order("deposit","DEPOSIT",0,0,amt,time));
        updateAllUI(); showMsg("Deposit Successful","$"+DF.format(amt)+" added. New balance: $"+DF.format(cash));
    }

    static void startTick(){
        ScheduledExecutorService ex=Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"PriceTick");t.setDaemon(true);return t;});
        ex.scheduleAtFixedRate(()->{
            STOCKS.values().forEach(s->{double mv=(RNG.nextDouble()-0.495)*0.003*s.price;s.price=Math.max(s.price+mv,1.0);s.change+=mv;s.pct=(s.change/(s.price-s.change))*100;s.hist.forEach((tf,list)->{list.add(s.price);if(list.size()>600)list.remove(0);});});
            SwingUtilities.invokeLater(()->{
                updateAllUI();
                if(activeScreen.equals("stocks")){
                    refreshStocksList();   // live price list
                    refreshDetailPanel();  // live chart + header
                }
            });
        },3,3,TimeUnit.SECONDS);
    }
    static void updateAllUI(){updateKPIs();renderDashActivity();renderDashMarket();if(activeScreen.equals("payment")){updatePayOrders();renderTxHistory();}}

    static class ChartView extends JPanel{
        List<Double> data=List.of(); boolean isUp=true;
        void setData(List<Double> d,boolean up){this.data=new ArrayList<>(d);this.isUp=up;repaint();}
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g); if(data.size()<2)return;
            Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(),h=getHeight(),pad=4,rpad=42,ch=h-20,cw=w-pad-rpad;
            double min=data.stream().mapToDouble(Double::doubleValue).min().orElse(0), max=data.stream().mapToDouble(Double::doubleValue).max().orElse(1), range=max-min; if(range==0)range=1;
            Color lc=isUp?GREEN:RED; int n=data.size(); int[] xs=new int[n],ys=new int[n];
            for(int i=0;i<n;i++){xs[i]=pad+(int)((double)i/(n-1)*cw);ys[i]=8+(int)((1-(data.get(i)-min)/range)*(ch-8));}
            GeneralPath fill=new GeneralPath(); fill.moveTo(xs[0],ys[0]); for(int i=1;i<n;i++)fill.lineTo(xs[i],ys[i]); fill.lineTo(xs[n-1],ch+8); fill.lineTo(xs[0],ch+8); fill.closePath();
            g2.setPaint(new GradientPaint(0,0,new Color(isUp?34:245,isUp?211:69,isUp?127:92,50),0,h,new Color(0,0,0,0))); g2.fill(fill);
            g2.setColor(lc); g2.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            for(int i=1;i<n;i++)g2.drawLine(xs[i-1],ys[i-1],xs[i],ys[i]);
            g2.setFont(new Font("Monospaced",Font.PLAIN,9));
            for(int i=0;i<=4;i++){double v=min+range*(1-(double)i/4);int y=8+(int)((double)i/4*(ch-8));g2.setColor(BORDER);g2.drawLine(pad,y,pad+cw,y);g2.setColor(TEXT3);g2.drawString("$"+(int)v,pad+cw+3,y+4);}
            g2.dispose();
        }
    }
    static class QRPanel extends JPanel{
        QRPanel(){setOpaque(true); setBackground(BG3);}
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BG3); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); g2.setColor(BORDER2); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
            int cell=8,off=10; Random r=new Random(42);
            for(int row=0;row<12;row++)for(int col=0;col<12;col++)if(r.nextBoolean()){g2.setColor(GOLD);g2.fillRect(off+col*cell,off+row*cell,cell-1,cell-1);}
            g2.setColor(GOLD); g2.fillRect(off,off,cell*3,cell*3); g2.setColor(BG3); g2.fillRect(off+cell,off+cell,cell,cell);
            g2.setColor(GOLD); g2.fillRect(off+8*cell,off,cell*3,cell*3); g2.setColor(BG3); g2.fillRect(off+9*cell,off+cell,cell,cell);
            g2.setColor(GOLD); g2.fillRect(off,off+8*cell,cell*3,cell*3); g2.setColor(BG3); g2.fillRect(off+cell,off+9*cell,cell,cell);
            g2.dispose();
        }
    }

    static JLabel lbl(String t,Font f,Color c){JLabel l=new JLabel(t);l.setFont(f);l.setForeground(c);return l;}
    static JLabel flbl(String t){JLabel l=lbl(t,F_TINYB,TEXT3);l.setAlignmentX(0f);return l;}
    static JLabel monoLbl(String t){return lbl(t,F_MONO,TEXT);}
    static JLabel link(String t){JLabel l=lbl(t,F_BODY2,GOLD);l.setCursor(handCursor());return l;}
    static JTextField field(String ph){JTextField f=new JTextField();f.setFont(F_BODY);f.setBackground(BG3);f.setForeground(TEXT);f.setCaretColor(TEXT);f.setBorder(new CompoundBorder(new LineBorder(BORDER2,1,true),new EmptyBorder(8,12,8,12)));return f;}
    static JPasswordField passField(String ph){JPasswordField f=new JPasswordField();f.setFont(F_BODY);f.setBackground(BG3);f.setForeground(TEXT);f.setCaretColor(TEXT);f.setBorder(new CompoundBorder(new LineBorder(BORDER2,1,true),new EmptyBorder(8,12,8,12)));return f;}
    static JButton goldBtn(String text){JButton b=new JButton(text);b.setFont(new Font("SansSerif",Font.BOLD,15));b.setBackground(GOLD);b.setForeground(Color.BLACK);b.setOpaque(true);b.setContentAreaFilled(true);b.setBorderPainted(false);b.setFocusPainted(false);b.setCursor(handCursor());return b;}
    static JButton tabBtn(String text,boolean active){JButton b=new JButton(text);b.setFont(new Font("SansSerif",Font.BOLD,13));b.setBackground(active?GOLD:BG2);b.setForeground(active?Color.BLACK:TEXT3);b.setOpaque(true);b.setBorder(new EmptyBorder(10,0,10,0));b.setFocusPainted(false);b.setCursor(handCursor());return b;}
    static void styleSpinner(JSpinner sp){sp.setFont(F_MONO);sp.setBackground(BG3);sp.setBorder(new CompoundBorder(new LineBorder(BORDER2,1,true),new EmptyBorder(4,8,4,8)));JSpinner.DefaultEditor ed=(JSpinner.DefaultEditor)sp.getEditor();ed.getTextField().setBackground(BG3);ed.getTextField().setForeground(TEXT);ed.getTextField().setFont(F_MONO);ed.getTextField().setCaretColor(TEXT);}
    static JScrollPane darkScroll(Component c){
        JScrollPane sp=new JScrollPane(c); sp.setBorder(null); sp.setOpaque(true); sp.setBackground(BG2);
        sp.getViewport().setOpaque(true); sp.getViewport().setBackground(BG2);
        sp.getVerticalScrollBar().setUnitIncrement(12);
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI(){@Override protected void configureScrollBarColors(){thumbColor=BG4;trackColor=BG2;}@Override protected JButton createDecreaseButton(int o){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}@Override protected JButton createIncreaseButton(int o){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}});
        return sp;
    }
    static JPanel miniStat(String l,JLabel v){v.setFont(F_MONOB);v.setForeground(TEXT);JPanel p=new JPanel(new GridLayout(2,1,0,4));p.setBackground(BG3);p.setBorder(new CompoundBorder(new LineBorder(BORDER2,1,true),new EmptyBorder(10,12,10,12)));p.add(lbl(l,F_TINYB,TEXT3));p.add(v);return p;}
    static JPanel hbox(int gap,Component...cs){JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,gap,0));p.setOpaque(false);for(Component c:cs)p.add(c);return p;}
    static JPanel rAlign(JLabel l){JPanel p=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));p.setOpaque(false);p.add(l);return p;}
    static Component vs(int h){return Box.createVerticalStrut(h);}
    static void setMaxH(JComponent c,int h){c.setMaximumSize(new Dimension(Integer.MAX_VALUE,h));}
    static Cursor handCursor(){return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);}
    static void showMsg(String title,String msg){JOptionPane.showMessageDialog(frame,msg,title,JOptionPane.INFORMATION_MESSAGE);}
    static void showToast(String title,String msg,boolean success){
        if(toastPanel==null||toastTitle==null||toastMsg==null||toastIcon==null)return;
        boolean isErr=title.toLowerCase().contains("error")||title.toLowerCase().contains("invalid");
        Color accent=isErr?RED:success?GREEN:GOLD; String icon=isErr?"\u2717":success?"\u2713":"i";
        toastIcon.setText(icon); toastIcon.setForeground(accent); toastTitle.setText(title); toastTitle.setForeground(TEXT);
        String fl=msg.contains("\n")?msg.substring(0,msg.indexOf("\n")):msg; toastMsg.setText(fl); toastMsg.setForeground(TEXT2);
        positionToast();
        if(frame!=null&&frame.getGlassPane()!=null)frame.getGlassPane().setVisible(true);
        toastPanel.setVisible(true); toastPanel.repaint();
        if(toastTimer!=null&&toastTimer.isRunning())toastTimer.stop();
        toastTimer=new javax.swing.Timer(3500,e->{toastPanel.setVisible(false);if(frame!=null&&frame.getGlassPane()!=null)frame.getGlassPane().setVisible(false);});
        toastTimer.setRepeats(false); toastTimer.start();
    }
    static String fmtK(long n){if(n>=1_000_000_000)return String.format("%.1fB",n/1e9);if(n>=1_000_000)return String.format("%.1fM",n/1e6);if(n>=1_000)return String.format("%.1fK",n/1e3);return String.valueOf(n);}
    static String fmtCardNum(String raw){StringBuilder sb=new StringBuilder();int len=Math.min(raw.length(),16);for(int i=0;i<len;i++){if(i>0&&i%4==0)sb.append(' ');sb.append(raw.charAt(i));}while(sb.toString().replaceAll(" ","").length()<16){if(sb.length()>0&&sb.length()%5==4)sb.append(' ');sb.append('\u2022');}return sb.toString();}
    static class MA extends MouseAdapter{private final Runnable r;MA(Runnable r){this.r=r;}@Override public void mouseClicked(MouseEvent e){r.run();}}
    static class DocL implements DocumentListener{private final Runnable r;DocL(Runnable r){this.r=r;}public void insertUpdate(DocumentEvent e){r.run();}public void removeUpdate(DocumentEvent e){r.run();}public void changedUpdate(DocumentEvent e){r.run();}}
}