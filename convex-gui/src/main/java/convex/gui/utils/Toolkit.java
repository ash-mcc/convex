package convex.gui.utils;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.DefaultEditorKit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import convex.core.util.Utils;
import convex.gui.components.AddressCombo;
import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.AbstractMaterialTheme;
import mdlaf.themes.MaterialOceanicTheme;

public class Toolkit {

	private static Logger log = LoggerFactory.getLogger(Toolkit.class.getName());

	public static Font DEFAULT_FONT = new JLabel().getFont();

	public static Font MONO_FONT = new Font(Font.MONOSPACED, Font.BOLD, 20);

	public static Font SMALL_MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);
	public static Font SMALL_MONO_BOLD = SMALL_MONO_FONT.deriveFont(Font.BOLD);

	
	static {
		
		
		try {
			UIManager.installLookAndFeel("Material", "mdlaf.MaterialLookAndFeel");
			Class.forName("mdlaf.MaterialLookAndFeel");
			// search for Nimbus look and feel if it is available
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				String name = info.getName();
				// log.info("Found L&F: " + name);
				if (name.equals("Nimbus")) { // Nimbus
					UIManager.setLookAndFeel(info.getClassName());
					// UIManager.put("nimbusBase", new Color(130,89,171));
					// UIManager.put("menu", new Color(61,89,171));
					// UIManager.put("control", new Color(200,180,160));
				}
			}
			
			// prefer MaterialLookAndFeel if we have it
			AbstractMaterialTheme theme = new MaterialOceanicTheme();
			MaterialLookAndFeel material = new MaterialLookAndFeel(theme);
			UIManager.setLookAndFeel(material);
			DEFAULT_FONT=theme.getFontRegular().deriveFont(14.0f);
			theme.setFontRegular(new FontUIResource(DEFAULT_FONT.deriveFont(14.0f)));
			theme.setFontBold(new FontUIResource(theme.getFontBold().deriveFont(14.0f)));
			theme.setFontItalic(new FontUIResource(theme.getFontItalic().deriveFont(14.0f)));
			theme.setFontMedium(new FontUIResource(theme.getFontMedium().deriveFont(14.0f)));
			

			UIManager.getLookAndFeelDefaults().put("TextField.caretForeground", Color.white);
			
			if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
				InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
				im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
				im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
				im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
			}
			
			InputStream is = Utils.getResourceAsStream("fonts/SourceCodePro-Regular.ttf");
			Font monoFont = Font.createFont(Font.TRUETYPE_FONT, is);
			SMALL_MONO_FONT = monoFont.deriveFont(14f);
			SMALL_MONO_BOLD = SMALL_MONO_FONT.deriveFont(Font.BOLD);
		} catch (Exception e) {
			e.printStackTrace();
			log.warn("Unable to set look and feel: {}", e);
		}
	}

	// public static final ImageIcon LOCKED_ICON =
	// scaledIcon(36,"/images/ic_lock_outline_black_36dp.png");
	// public static final ImageIcon UNLOCKED_ICON =
	// scaledIcon(36,"/images/ic_lock_open_black_36dp.png");

	public static final ImageIcon LOCKED_ICON = scaledIcon(36, "/images/padlock.png");
	public static final ImageIcon UNLOCKED_ICON = scaledIcon(36, "/images/padlock-open.png");
	public static final ImageIcon WARNING = scaledIcon(36, "/images/ic_priority_high_black_36dp.png");
	public static final ImageIcon CAKE = scaledIcon(36, "/images/ic_cake_black_36dp.png");
	public static final ImageIcon CONVEX = scaledIcon(36, "/images/Convex.png");
	public static final ImageIcon COG = scaledIcon(36, "/images/cog.png");
	public static final ImageIcon REPL_ICON = scaledIcon(36, "/images/terminal-icon.png");

	public static final ImageIcon TESTNET_ICON = scaledIcon(128, "/images/testnet.png");
	public static final ImageIcon WWW_ICON = scaledIcon(128, "/images/www.png");
	public static final ImageIcon HACKER_ICON = scaledIcon(128, "/images/hacker.png");
	public static final ImageIcon TERMINAL_ICON = scaledIcon(128, "/images/terminal.png");
	public static final ImageIcon ECOSYSTEM_ICON = scaledIcon(128, "/images/ecosystem.png");
	public static final ImageIcon WALLET_ICON = scaledIcon(128, "/images/wallet.png");

	public static ImageIcon scaledIcon(int size, String resourcePath) {
		java.net.URL imgURL = Toolkit.class.getResource(resourcePath);
		if (imgURL == null) throw new Error("No image: " + resourcePath);
		ImageIcon imageIcon = new ImageIcon(imgURL);
		Image image = imageIcon.getImage();
		image = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);

		return new ImageIcon(image);
	}

	/**
	 * Scale an image with interpolation / AA for nicer effects
	 * 
	 * @param src Source image
	 * @param w Width of new image
	 * @param h Height of new image
	 * @return A new, resized image
	 */
	public static BufferedImage smoothResize(BufferedImage src, int w, int h) {
		BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) newImage.getGraphics();

		// set up rendering hints
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		graphics.drawImage(src, 0, 0, w, h, null);
		return newImage;
	}

	public static DocumentListener createDocumentListener(final Runnable a) {
		return new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(a);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(a);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(a);
			}
		};
	}

	public static void init() {
		// Empty method, just triggers static initialisation
	}

	public static Image getImage(URL resourceURL) {
		return getDefaultToolkit().getImage(resourceURL);
	}

	public static java.awt.Toolkit getDefaultToolkit() {
		return java.awt.Toolkit.getDefaultToolkit();
	}

	public static void launchBrowser(String url) {
		try {
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Desktop.getDesktop().browse(new URI(url));
			}
		} catch (IOException | URISyntaxException ex) {
			log.warn("IO Failure launching browser: "+ex);
		}
	}

	public static void showMainFrame(JComponent comp) {
		 JFrame frame = new JFrame("Test Frame"); 
		 frame.getContentPane().add(comp);
		 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 frame.pack();
		 frame.setVisible(true);
	}
}
