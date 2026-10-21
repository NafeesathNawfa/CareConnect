package com.careconnect.ui.screens;

import com.careconnect.dao.DAOFactory;
import com.careconnect.model.Patient;
import com.careconnect.model.Staff;
import com.careconnect.ui.components.*;
import com.careconnect.ui.theme.StatusBanner;
import com.careconnect.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Redesigned login experience for CareConnect.
 *
 * A single centered card (~480px) holds a Patient Portal / Staff & Clinician
 * segmented toggle. One shared toggle drives one shared {@link CardLayout},
 * so the selected tab and the visible form can never disagree.
 *
 * Behaviour is unchanged: patient login by phone, "Register Online", and staff
 * authentication all call the same DAO methods and the same
 * {@link LoginCallback} hooks as before. No business logic lives here.
 */
public class WelcomeLoginScreen extends JPanel {

    public interface LoginCallback {
        void onPatientLogin(Patient patient);
        void onStaffLogin(Staff staff);
        void onOpenPatientRegistration();
    }

    private static final int CARD_WIDTH = 480;
    /** Inner padding of the card on all four sides (keeps content off the edge). */
    private static final int CARD_PAD = 36;

    private final LoginCallback callback;

    // One deck of forms, one toggle controlling it.
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel formDeck = new JPanel(cardLayout);
    private SegmentedToggle modeToggle;

    // Patient form state
    private RoundedTextField patientPhoneField;
    private JLabel patientErrorLabel;
    private StatusBanner patientBanner;

    // Staff form state
    private RoundedTextField staffUsernameField;
    private JPasswordField staffPasswordField;
    private JLabel staffErrorLabel;
    private StatusBanner staffBanner;

    public WelcomeLoginScreen(LoginCallback callback) {
        this.callback = callback;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        add(LoginComponents.topBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        showMode(0);
    }

    // ==================================================================
    // Centered single-card layout
    // ==================================================================
    private JPanel buildCenter() {
        // A single column that centers the fixed-width card horizontally and
        // pins it to the top; when the viewport is taller, extra space is a
        // filler row that keeps the card reachable by scrolling, never clipped.
        JPanel column = new JPanel(new GridBagLayout());
        column.setBackground(Theme.BG);

        card = buildCard();
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fixed-width wrapper so the card does not stretch to the window width.
        JPanel cardRow = new JPanel(new GridBagLayout());
        cardRow.setOpaque(false);
        GridBagConstraints inner = new GridBagConstraints();
        inner.gridx = 0;
        inner.gridy = 0;
        inner.weightx = 1;
        cardRow.add(card, inner);
        cardRow.setMaximumSize(new Dimension(CARD_WIDTH + CARD_PAD * 2, Integer.MAX_VALUE));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.NORTH;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        column.add(cardRow, gc);

        gc.gridy = 1;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        column.add(spacer, gc);

        // Transparent, borderless scroll pane: scrolls only when needed.
        JScrollPane scroll = new JScrollPane(column,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel host = new JPanel(new BorderLayout());
        host.setBackground(Theme.BG);
        host.setBorder(new EmptyBorder(Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG, Theme.SPACE_LG));
        host.add(scroll, BorderLayout.CENTER);
        return host;
    }

    private RoundedPanel card;

    private RoundedPanel buildCard() {
        RoundedPanel card = RoundedPanel.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        // 36px inner padding on all four sides (theme SPACE_XL + a little more
        // at the bottom so the last element never touches the border).
        card.setBorder(new EmptyBorder(CARD_PAD, CARD_PAD, CARD_PAD, CARD_PAD));

        // Scheduled when the toggle measures itself; the deck's height is fixed
        // to the taller form so switching tabs never changes the card height.
        modeToggle = new SegmentedToggle("Patient Portal", "Staff / Clinician");
        modeToggle.setAlignmentX(Component.LEFT_ALIGNMENT);
        modeToggle.setOnChange(this::showMode);

        JPanel patientForm = buildPatientForm();
        JPanel staffForm = buildStaffForm();

        formDeck.setOpaque(false);
        formDeck.setLayout(cardLayout);
        formDeck.setAlignmentX(Component.LEFT_ALIGNMENT);
        formDeck.add(patientForm, "patient");
        formDeck.add(staffForm, "staff");

        this.patientForm = patientForm;
        this.staffForm = staffForm;

        card.add(modeToggle);
        card.add(strut(Theme.SPACE_XL));
        card.add(formDeck);

        // Fix the deck to the taller form whenever fonts/size change.
        installDeckHeightSync(patientForm, staffForm);

        return card;
    }

    private JPanel patientForm;
    private JPanel staffForm;

    /**
     * Keeps the CardLayout deck pinned to the taller form's preferred height so
     * the card height is identical on both tabs. Recomputes when the window or
     * fonts change.
     */
    private void installDeckHeightSync(JPanel patientForm, JPanel staffForm) {
        Runnable sync = () -> {
            int h = Math.max(patientForm.getPreferredSize().height,
                    staffForm.getPreferredSize().height);
            int w = CARD_WIDTH - CARD_PAD * 2;
            formDeck.setPreferredSize(new Dimension(w, h));
            formDeck.setMinimumSize(new Dimension(w, h));
            formDeck.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
            formDeck.revalidate();
            formDeck.repaint();
        };
        sync.run();
        patientForm.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) { sync.run(); }
        });
        staffForm.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) { sync.run(); }
        });
    }

    /**
     * A title block whose structure (font, sizes, spacing) is identical on both
     * tabs, so only the text differs and nothing shifts when switching.
     */
    private JPanel titleBlock(String title, String subtitle) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);
        block.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.add(titleLabel);
        block.add(strut(6));

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(Theme.FONT_BODY);
        subtitleLabel.setForeground(Theme.TEXT_HINT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.add(subtitleLabel);
        block.add(strut(Theme.SPACE_LG));

        return block;
    }

    /**
     * A left-aligned vertical strut. BoxLayout offsets every child when their
     * alignmentX values differ, so all struts must be LEFT_ALIGNMENT too.
     */
    private static Component strut(int height) {
        JComponent c = (JComponent) Box.createVerticalStrut(height);
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    /** Single entry point that keeps the toggle and the visible form in sync. */
    private void showMode(int index) {
        if (modeToggle != null) modeToggle.setSelectedIndex(index);
        cardLayout.show(formDeck, index == 0 ? "patient" : "staff");
    }

    // ==================================================================
    // PATIENT FORM
    // ==================================================================
    private JPanel buildPatientForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        form.add(titleBlock("Patient Sign In",
                "Enter your registered phone number to continue."));

        patientBanner = new StatusBanner();
        patientBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(patientBanner);
        form.add(strut(Theme.SPACE_SM));

        JPanel labelRow = new JPanel(new BorderLayout());
        labelRow.setOpaque(false);
        labelRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        JLabel fieldLabel = new JLabel("Registered Phone Number");
        fieldLabel.setFont(Theme.FONT_LABEL);
        fieldLabel.setForeground(Theme.TEXT_BODY);
        JLabel hint = new JLabel("Demo: 8000000001");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_HINT);
        labelRow.add(fieldLabel, BorderLayout.WEST);
        labelRow.add(hint, BorderLayout.EAST);
        form.add(labelRow);
        form.add(strut(6));

        patientPhoneField = new RoundedTextField("Enter phone number", IconPainter.PHONE);
        patientPhoneField.setAlignmentX(Component.LEFT_ALIGNMENT);
        patientPhoneField.setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.FIELD_HEIGHT));
        patientPhoneField.addActionListener(e -> handlePatientLogin());
        form.add(patientPhoneField);
        form.add(strut(4));

        patientErrorLabel = new JLabel(" ");
        patientErrorLabel.setFont(Theme.FONT_SMALL);
        patientErrorLabel.setForeground(Theme.DANGER);
        patientErrorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(patientErrorLabel);
        form.add(strut(Theme.SPACE_MD));

        PrimaryButton signIn = new PrimaryButton("Sign In as Patient", PrimaryButton.Variant.PRIMARY,
                IconPainter.ARROW_RIGHT_IN, null);
        signIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.BUTTON_HEIGHT));
        signIn.addActionListener(e -> handlePatientLogin());
        form.add(signIn);
        form.add(strut(Theme.SPACE_LG));

        form.add(divider());
        form.add(strut(Theme.SPACE_MD));
        form.add(registerBox());
        return form;
    }

    private JPanel registerBox() {
        RoundedPanel box = RoundedPanel.flat(Theme.INFO_BG, 12);
        box.setLayout(new BorderLayout(12, 0));
        box.setBorder(new EmptyBorder(14, 16, 14, 16));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        JLabel title = new JLabel("New Patient?");
        title.setFont(Theme.FONT_LABEL);
        title.setForeground(Theme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("<html><body style='width:150px'>Enroll once to receive your free community health ID card.</body></html>");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_HINT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(title);
        text.add(strut(2));
        text.add(sub);

        PrimaryButton register = new PrimaryButton("Register Online", PrimaryButton.Variant.SECONDARY, null, IconPainter.ARROW_RIGHT_IN);
        register.setPreferredSize(new Dimension(170, Theme.BUTTON_HEIGHT));
        register.addActionListener(e -> {
            if (callback != null) callback.onOpenPatientRegistration();
        });

        box.add(text, BorderLayout.CENTER);
        box.add(register, BorderLayout.EAST);
        return box;
    }

    // ==================================================================
    // STAFF FORM
    // ==================================================================
    private JPanel buildStaffForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        form.add(titleBlock("Staff & Provider Login",
                "Provide your institutional credentials to continue."));

        staffBanner = new StatusBanner();
        staffBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(staffBanner);
        form.add(strut(Theme.SPACE_SM));

        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(Theme.FONT_LABEL);
        userLabel.setForeground(Theme.TEXT_BODY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(userLabel);
        form.add(strut(6));
        staffUsernameField = new RoundedTextField("e.g. admin or reception", IconPainter.USER);
        staffUsernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        staffUsernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.FIELD_HEIGHT));
        form.add(staffUsernameField);
        form.add(strut(Theme.SPACE_MD));

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(Theme.FONT_LABEL);
        passLabel.setForeground(Theme.TEXT_BODY);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(passLabel);
        form.add(strut(6));
        staffPasswordField = new JPasswordField();
        staffPasswordField.setFont(Theme.FONT_BODY);
        staffPasswordField.setOpaque(false);
        staffPasswordField.setForeground(Theme.TEXT_BODY);
        staffPasswordField.setCaretColor(Theme.PRIMARY);
        staffPasswordField.setBorder(BorderFactory.createEmptyBorder(0, 44, 0, 14));
        staffPasswordField.addActionListener(e -> handleStaffLogin());
        RoundedPasswordWrap passWrap = new RoundedPasswordWrap(staffPasswordField);
        passWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        passWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.FIELD_HEIGHT));
        passWrap.setPreferredSize(new Dimension(0, Theme.FIELD_HEIGHT));
        form.add(passWrap);
        form.add(strut(4));

        staffErrorLabel = new JLabel(" ");
        staffErrorLabel.setFont(Theme.FONT_SMALL);
        staffErrorLabel.setForeground(Theme.DANGER);
        staffErrorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(staffErrorLabel);
        form.add(strut(Theme.SPACE_MD));

        PrimaryButton auth = new PrimaryButton("Authenticate & Enter Console", PrimaryButton.Variant.PRIMARY,
                IconPainter.LOCK, null);
        auth.setAlignmentX(Component.LEFT_ALIGNMENT);
        auth.setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.BUTTON_HEIGHT));
        auth.addActionListener(e -> handleStaffLogin());
        form.add(auth);
        form.add(strut(Theme.SPACE_MD));

        JLabel note = new JLabel("<html><body style='width:380px'>Authentication is validated against the staff "
                + "accounts stored in the clinic database (username and role).</body></html>");
        note.setFont(Theme.FONT_SMALL);
        note.setForeground(Theme.TEXT_HINT);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(note);
        return form;
    }

    /** Wraps a JPasswordField so it can share the rounded input styling. */
    private static class RoundedPasswordWrap extends JPanel {
        RoundedPasswordWrap(JPasswordField field) {
            setOpaque(false);
            setLayout(new BorderLayout());
            add(field, BorderLayout.CENTER);
            field.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) { repaint(); }
                @Override public void focusLost(java.awt.event.FocusEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int r = Theme.RADIUS_CONTROL;
            g2.setColor(Theme.INPUT_FILL);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), r, r);
            boolean focused = getComponents().length > 0 && getComponent(0).hasFocus();
            g2.setColor(focused ? Theme.INPUT_BORDER_FOCUS : Theme.INPUT_BORDER);
            g2.setStroke(new BasicStroke(focused ? 1.6f : 1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, r, r);
            Graphics2D ig = (Graphics2D) g2.create();
            ig.translate(14, (getHeight() - 18) / 2);
            ig.setColor(Theme.TEXT_HINT);
            ig.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            IconPainter.LOCK.paint(ig, 18);
            ig.dispose();
            g2.dispose();
        }
    }

    private JPanel divider() {
        JPanel d = new JPanel();
        d.setOpaque(false);
        d.setAlignmentX(Component.LEFT_ALIGNMENT);
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        d.setPreferredSize(new Dimension(0, 1));
        d.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.CARD_BORDER));
        return d;
    }

    // ==================================================================
    // Behaviour (unchanged DAO calls)
    // ==================================================================
    private void handlePatientLogin() {
        patientBanner.clear();
        patientErrorLabel.setText(" ");
        patientPhoneField.markError(false);

        String phone = patientPhoneField.getText().trim();
        if (phone.isEmpty()) {
            patientErrorLabel.setText("Please enter your registered phone number.");
            patientPhoneField.markError(true);
            patientPhoneField.requestFocus();
            return;
        }

        try {
            Patient patient = DAOFactory.getInstance().getPatientDAO().getPatientByPhone(phone);
            if (patient != null) {
                if (callback != null) callback.onPatientLogin(patient);
            } else {
                patientErrorLabel.setText("No registered patient found with phone number " + phone + ".");
                patientPhoneField.markError(true);
                patientBanner.showMessage("Please click 'Register Online' to enroll as a new patient.", StatusBanner.Type.ERROR);
            }
        } catch (Exception ex) {
            patientBanner.showMessage("Error verifying patient: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    private void handleStaffLogin() {
        staffBanner.clear();
        staffErrorLabel.setText(" ");

        String username = staffUsernameField.getText().trim();
        String password = new String(staffPasswordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            staffErrorLabel.setText("Please enter both username and password.");
            return;
        }

        try {
            Staff staff = DAOFactory.getInstance().getStaffDAO().authenticate(username, password);
            if (staff != null) {
                if (callback != null) callback.onStaffLogin(staff);
            } else {
                staffErrorLabel.setText("Invalid staff credentials.");
                staffBanner.showMessage("Try username 'admin' and password 'admin123'.", StatusBanner.Type.ERROR);
            }
        } catch (Exception ex) {
            staffBanner.showMessage("Authentication error: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }
}
