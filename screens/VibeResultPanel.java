package screens;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import model.MbtiResult;
import model.User;
import service.MbtiService;
import ui.RoundedButton;
import ui.RoundedPanel;
import ui.Theme;
import ui.UiHelper;

/*
 * Shows the result of the vibe test: a colorful orb, the archetype name and a
 * short blurb, plus four little bars for the personality axes. From here they
 * head into the app to see their matched communities.
 */
public class VibeResultPanel extends JPanel {

    private AppFrame appFrame;
    private User user;
    private MbtiResult result;
    private MbtiService mbti = new MbtiService();

    public VibeResultPanel(AppFrame appFrame, User user, MbtiResult result) {
        this.appFrame = appFrame;
        this.user = user;
        this.result = result;

        setBackground(Theme.BG);
        setLayout(new java.awt.GridBagLayout());

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setPreferredSize(new Dimension(440, 560));

        JLabel kicker = UiHelper.muted("YOUR VIBE IS", 12);
        kicker.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(kicker);
        col.add(UiHelper.vgap(14));

        Orb orb = new Orb(mbti.archetypeEmoji(result.getType()));
        orb.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(orb);
        col.add(UiHelper.vgap(16));

        JLabel name = UiHelper.title(result.getType() + " · " + mbti.archetypeName(result.getType()), 24);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setHorizontalAlignment(SwingConstants.CENTER);
        col.add(name);
        col.add(UiHelper.vgap(8));

        JLabel blurb = new JLabel("<html><div style='text-align:center;width:400px'>"
                + mbti.archetypeBlurb(result.getType()) + "</div></html>");
        blurb.setFont(Theme.body(14));
        blurb.setForeground(Theme.INK_SOFT);
        blurb.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(blurb);
        col.add(UiHelper.vgap(22));

        // the four axis bars
        col.add(axisRow(result.getType().charAt(0) == 'E' ? "Extraversion" : "Introversion",
                strength(result.getEiPercent(), result.getType().charAt(0) == 'E')));
        col.add(axisRow(result.getType().charAt(1) == 'S' ? "Sensing" : "Intuition",
                strength(result.getSnPercent(), result.getType().charAt(1) == 'S')));
        col.add(axisRow(result.getType().charAt(2) == 'T' ? "Thinking" : "Feeling",
                strength(result.getTfPercent(), result.getType().charAt(2) == 'T')));
        col.add(axisRow(result.getType().charAt(3) == 'J' ? "Judging" : "Perceiving",
                strength(result.getJpPercent(), result.getType().charAt(3) == 'J')));

        col.add(UiHelper.vgap(20));

        RoundedButton go = UiHelper.primaryButton("See my communities  →");
        go.setAlignmentX(Component.CENTER_ALIGNMENT);
        go.setPreferredSize(new Dimension(300, 46));
        go.setMaximumSize(new Dimension(300, 46));
        go.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                appFrame.enterApp(user);
            }
        });
        col.add(go);

        add(col);
    }

    // strength toward the pole they actually got (always >= 50)
    private int strength(int pctTowardFirst, boolean gotFirst) {
        if (gotFirst) {
            return pctTowardFirst;
        }
        return 100 - pctTowardFirst;
    }

    private JPanel axisRow(String label, int pct) {
        RoundedPanel row = new RoundedPanel(12, Theme.WHITE);
        row.setLayout(new java.awt.BorderLayout(12, 0));
        row.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 14, 10, 14));
        row.setMaximumSize(new Dimension(440, 44));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel(label);
        name.setFont(Theme.bodyBold(13));
        name.setForeground(Theme.INK);
        name.setPreferredSize(new Dimension(110, 20));
        row.add(name, java.awt.BorderLayout.WEST);

        // the little bar
        RoundedPanel track = new RoundedPanel(5, Theme.LILAC_100);
        track.setLayout(null);
        final int trackW = 220;
        track.setPreferredSize(new Dimension(trackW, 8));
        RoundedPanel fill = new RoundedPanel(5, Theme.LILAC_500);
        fill.setBounds(0, 0, (int) (trackW * (pct / 100.0)), 8);
        track.add(fill);
        JPanel center = new JPanel(new java.awt.GridBagLayout());
        center.setOpaque(false);
        center.add(track);
        row.add(center, java.awt.BorderLayout.CENTER);

        JLabel pctLabel = new JLabel(pct + "");
        pctLabel.setFont(Theme.bodyBold(13));
        pctLabel.setForeground(Theme.LILAC_600);
        row.add(pctLabel, java.awt.BorderLayout.EAST);

        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new java.awt.BorderLayout());
        wrap.setMaximumSize(new Dimension(440, 52));
        wrap.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        wrap.add(row);
        return wrap;
    }

    /*
     * The gradient circle with the emoji in the middle. Painted by hand because
     * swing has no "circle with a soft glow" component.
     */
    private static class Orb extends JPanel {
        private String emoji;

        Orb(String emoji) {
            this.emoji = emoji;
            setOpaque(false);
            setPreferredSize(new Dimension(140, 140));
            setMaximumSize(new Dimension(140, 140));
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int d = Math.min(getWidth(), getHeight());
            float r = d / 2f;
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            // soft gradient from blush center out to lilac edge
            float[] stops = { 0f, 1f };
            Color[] colors = { Theme.BLUSH, Theme.LILAC_400 };
            RadialGradientPaint paint = new RadialGradientPaint(cx, cy, r, stops, colors);
            g2.setPaint(paint);
            g2.fillOval((int) (cx - r), (int) (cy - r), d, d);

            // emoji in the middle
            g2.setFont(new Font("SansSerif", Font.PLAIN, 52));
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(emoji);
            g2.drawString(emoji, (int) (cx - tw / 2), (int) (cy + fm.getAscent() / 2 - 6));
            g2.dispose();
        }
    }
}
