package com.careconnect.ui.screens;

import com.careconnect.dao.DAOFactory;
import com.careconnect.model.Patient;
import com.careconnect.ui.components.IconLabel;
import com.careconnect.ui.components.IconPainter;
import com.careconnect.ui.components.PrimaryButton;
import com.careconnect.ui.components.RoundedComboBox;
import com.careconnect.ui.components.RoundedPanel;
import com.careconnect.ui.components.RoundedTextField;
import com.careconnect.ui.components.SecondaryButton;
import com.careconnect.ui.theme.StatusBanner;
import com.careconnect.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Patient Registration screen (redesigned to match the login experience).
 *
 * Behaviour is unchanged: the same fields, validation rules/messages, DAO
 * registration call, success dialog and post-registration callback are kept.
 * Only the presentation layer changed.
 */
public class PatientRegistrationScreen extends JPanel {

    public interface RegistrationCallback {
        void onRegistrationSuccess(Patient registeredPatient);
        void onBackToLogin();
    }

    /** Outer card width (including the shadow inset painted by RoundedPanel). */
    private static final int CARD_WIDTH = 860;
    private static final int LABEL_COLUMN_WIDTH = 200;
    /** Fixed height for the hint/error line under each input (no layout shift). */
    private static final int MESSAGE_SLOT_HEIGHT = 18;

    private final RegistrationCallback callback;
    private final StatusBanner statusBanner;

    private final RoundedTextField nameField;
    private final RoundedTextField dobField;
    private final RoundedComboBox<String> genderCombo;
    private final RoundedTextField phoneField;
    private final RoundedTextField emailField;
    private final RoundedTextField addressField;

    private final JLabel nameError;
    private final JLabel dobError;
    private final JLabel phoneError;
    private final JLabel emailError;

    private final CardLayout phoneMessageCards;
    private final JPanel phoneMessageSlot;

    public PatientRegistrationScreen(RegistrationCallback callback) {
        this.callback = callback;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        nameField = new RoundedTextField("Full legal name (e.g. Maya Sharma)");
        dobField = new RoundedTextField("YYYY-MM-DD (e.g. 1995-08-20)");
        dobField.setTrailingIcon(IconPainter.CALENDAR);

        // Keep the existing option values exactly as before.
        genderCombo = new RoundedComboBox<>(new String[]{"FEMALE", "MALE", "OTHER"});

        phoneField = new RoundedTextField("10-digit phone number (e.g. 9876543210)");
        emailField = new RoundedTextField("Email address (optional)");
        addressField = new RoundedTextField("Residential city / address");

        nameError = createErrorLabel();
        dobError = createErrorLabel();
        phoneError = createErrorLabel();
        emailError = createErrorLabel();

        phoneMessageCards = new CardLayout();
        phoneMessageSlot = new JPanel(phoneMessageCards);
        phoneMessageSlot.setOpaque(false);
        phoneMessageSlot.add(phoneHintRow(), "hint");
        phoneMessageSlot.add(phoneError, "error");
        phoneMessageCards.show(phoneMessageSlot, "hint");
        fixSlotHeight(phoneMessageSlot);

        statusBanner = new StatusBanner();

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        installBehaviours();
    }

    // ==================================================================
    // Header
    // ==================================================================
    private JComponent buildHeader() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // Soft bottom shadow, faded over the last 5 pixels.
                for (int i = 0; i < 5; i++) {
                    g2.setColor(Theme.shadowAt(Math.max(0, 14 - i * 3) / 255f));
                    g2.drawLine(0, getHeight() - 1 - i, getWidth(), getHeight() - 1 - i);
                }
                g2.setColor(Theme.CARD_BORDER);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        bar.setBackground(Theme.CARD);
        bar.setBorder(new EmptyBorder(0, Theme.SPACE_XL, 0, Theme.SPACE_XL));
        bar.setPreferredSize(new Dimension(0, 80));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.setOpaque(false);

        JPanel logo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.translate(getWidth() / 2 - 13, getHeight() / 2 - 13);
                IconPainter.CROSS.paint(g2, 26);
                g2.dispose();
            }
        };
        logo.setOpaque(false);
        logo.setPreferredSize(new Dimension(40, 40));
        logo.setMinimumSize(new Dimension(40, 40));
        logo.setMaximumSize(new Dimension(40, 40));
        logo.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel brand = new JLabel("CareConnect");
        brand.setFont(Theme.FONT_HEADING);
        brand.setForeground(Theme.PRIMARY);
        brand.setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel divider = new JPanel();
        divider.setBackground(Theme.CARD_BORDER);
        divider.setOpaque(true);
        divider.setPreferredSize(new Dimension(1, 32));
        divider.setMinimumSize(new Dimension(1, 32));
        divider.setMaximumSize(new Dimension(1, 32));
        divider.setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("New Patient Registration");
        title.setFont(Theme.FONT_TITLE.deriveFont(Font.BOLD, 24f));
        title.setForeground(Theme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Enter your details to create a clinic account.");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_HINT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subtitle);

        left.add(logo);
        left.add(Box.createHorizontalStrut(12));
        left.add(brand);
        left.add(Box.createHorizontalStrut(20));
        left.add(divider);
        left.add(Box.createHorizontalStrut(20));
        left.add(titleBlock);

        SecondaryButton back = new SecondaryButton("Back to Login", IconPainter.ARROW_LEFT, null);
        back.addActionListener(e -> {
            if (callback != null) callback.onBackToLogin();
        });

        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        right.add(back);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ==================================================================
    // Centered, scrollable card
    // ==================================================================
    private JComponent buildCenter() {
        JPanel column = new JPanel(new GridBagLayout());
        column.setBackground(Theme.BG);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.NORTH;
        gc.fill = GridBagConstraints.NONE;
        column.add(buildCard(), gc);

        gc.gridy = 1;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        column.add(spacer, gc);

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

    private RoundedPanel buildCard() {
        RoundedPanel card = new RoundedPanel(Theme.CARD, Theme.RADIUS_CARD, true, false) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                // Width is fixed by the design; height always comes from content.
                return new Dimension(CARD_WIDTH, d.height);
            }
        };
        card.setBorderColor(Theme.CARD_BORDER);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(0, 0, 16, 0);
        card.add(statusBanner, gc);

        gc.gridy = 1;
        gc.insets = new Insets(0, 0, 0, 0);
        card.add(buildForm(), gc);
        return card;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        int row = 0;
        addRow(form, row++, "Full Name", true, nameField, messageSlot(nameError, null));
        addRow(form, row++, "Date of Birth (YYYY-MM-DD)", true, dobField, messageSlot(dobError, null));
        addRow(form, row++, "Gender", true, genderCombo, messageSlot(createErrorLabel(), null));
        addRow(form, row++, "Phone Number", true, phoneField, phoneMessageSlot);
        addRow(form, row++, "Email Address", false, emailField, messageSlot(emailError, null));
        addRow(form, row++, "Address", false, addressField, messageSlot(createErrorLabel(), null));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = row++;
        gc.gridwidth = 2;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 0, 0, 0);
        form.add(divider(), gc);

        gc.gridy = row;
        gc.insets = new Insets(16, 0, 0, 0);
        form.add(buttonRow(), gc);
        return form;
    }

    private void addRow(JPanel form, int row, String labelText, boolean required,
                        JComponent input, JPanel messageSlot) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(0, 0, 0, 24);
        form.add(labelCell(labelText, required), gc);

        JPanel inputCell = new JPanel();
        inputCell.setOpaque(false);
        inputCell.setLayout(new BoxLayout(inputCell, BoxLayout.Y_AXIS));

        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.FIELD_HEIGHT));
        inputCell.add(input);
        messageSlot.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputCell.add(messageSlot);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 0, 0);
        form.add(inputCell, gc);
    }

    private JPanel labelCell(String text, boolean required) {
        JLabel label = new JLabel(required ? requiredLabel(text) : text);
        label.setFont(Theme.FONT_LABEL.deriveFont(Font.BOLD, 14f));
        label.setForeground(Theme.PRIMARY);

        // Align the label with the centre of the 48px input, ignoring any
        // hint/error line that sits below the input.
        int top = Math.max(0, (Theme.FIELD_HEIGHT - label.getPreferredSize().height) / 2);

        JPanel cell = new JPanel(new BorderLayout());
        cell.setOpaque(false);
        // Keep the column near the agreed 200px, but never clip a longer label
        // (for example the date-of-birth caption on wider system fonts).
        int width = Math.max(LABEL_COLUMN_WIDTH, label.getPreferredSize().width + 4);
        cell.setPreferredSize(new Dimension(width, Theme.FIELD_HEIGHT));
        cell.setBorder(new EmptyBorder(top, 0, 0, 0));
        cell.add(label, BorderLayout.NORTH);
        return cell;
    }

    private static String requiredLabel(String text) {
        return "<html>" + text + " <font color='" + hex(Theme.DANGER) + "'>*</font></html>";
    }

    private static String hex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(Theme.FONT_SMALL);
        label.setForeground(Theme.DANGER);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /** Reserves a fixed-height line so validation messages never shift the form. */
    private JPanel messageSlot(JLabel error, JComponent hintRow) {
        JPanel slot;
        if (hintRow != null) {
            CardLayout cards = new CardLayout();
            slot = new JPanel(cards);
            slot.add(hintRow, "hint");
            slot.add(error, "error");
            cards.show(slot, "hint");
        } else {
            slot = new JPanel(new BorderLayout());
            slot.add(error, BorderLayout.CENTER);
        }
        slot.setOpaque(false);
        fixSlotHeight(slot);
        return slot;
    }

    private static void fixSlotHeight(JPanel slot) {
        slot.setMinimumSize(new Dimension(10, MESSAGE_SLOT_HEIGHT));
        slot.setPreferredSize(new Dimension(10, MESSAGE_SLOT_HEIGHT));
        slot.setMaximumSize(new Dimension(Integer.MAX_VALUE, MESSAGE_SLOT_HEIGHT));
    }

    private JPanel phoneHintRow() {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);

        IconLabel info = new IconLabel(IconPainter.INFO, 14, Theme.TEXT_HINT);
        info.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Used to sign in to the patient portal.");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_HINT);
        hint.setAlignmentY(Component.CENTER_ALIGNMENT);

        row.add(info);
        row.add(Box.createHorizontalStrut(6));
        row.add(hint);
        row.add(Box.createHorizontalGlue());
        return row;
    }

    private JPanel divider() {
        JPanel d = new JPanel();
        d.setOpaque(false);
        d.setPreferredSize(new Dimension(0, 1));
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        d.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.CARD_BORDER));
        return d;
    }

    private JPanel buttonRow() {
        JPanel buttons = new JPanel(new GridBagLayout());
        buttons.setOpaque(false);

        SecondaryButton reset = new SecondaryButton("Reset Form");
        reset.setMinWidth(160);
        reset.addActionListener(e -> resetForm());

        PrimaryButton submit = new PrimaryButton("Complete Registration", PrimaryButton.Variant.PRIMARY,
                null, IconPainter.ARROW_RIGHT_IN);
        submit.setPreferredSize(new Dimension(240, Theme.BUTTON_HEIGHT));
        submit.setMinimumSize(new Dimension(240, Theme.BUTTON_HEIGHT));
        submit.addActionListener(e -> handleSubmit());

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.CENTER;
        buttons.add(Box.createHorizontalGlue(), gc);

        gc.gridx = 1;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        buttons.add(reset, gc);

        gc.gridx = 2;
        gc.insets = new Insets(0, 16, 0, 0);
        buttons.add(submit, gc);
        return buttons;
    }

    // ==================================================================
    // Behaviour (validation messages and DAO calls unchanged)
    // ==================================================================
    private void installBehaviours() {
        onEdit(nameField, () -> clearFieldError(nameField, nameError));
        onEdit(dobField, () -> clearFieldError(dobField, dobError));
        onEdit(phoneField, () -> {
            clearFieldError(phoneField, phoneError);
            phoneMessageCards.show(phoneMessageSlot, "hint");
        });
        onEdit(emailField, () -> clearFieldError(emailField, emailError));

        for (RoundedTextField field : new RoundedTextField[]{
                nameField, dobField, phoneField, emailField, addressField}) {
            field.addActionListener(e -> handleSubmit());
        }
    }

    private void onEdit(RoundedTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { action.run(); }
            @Override public void removeUpdate(DocumentEvent e) { action.run(); }
            @Override public void changedUpdate(DocumentEvent e) { action.run(); }
        });
    }

    private void clearFieldError(RoundedTextField field, JLabel error) {
        error.setText(" ");
        field.markError(false);
    }

    private void clearErrors() {
        clearFieldError(nameField, nameError);
        clearFieldError(dobField, dobError);
        clearFieldError(phoneField, phoneError);
        clearFieldError(emailField, emailError);
        phoneMessageCards.show(phoneMessageSlot, "hint");
    }

    private void showError(JLabel error, RoundedTextField field, String message) {
        error.setText(message);
        field.markError(true);
        field.requestFocus();
    }

    private void showPhoneError(String message) {
        phoneError.setText(message);
        phoneField.markError(true);
        phoneMessageCards.show(phoneMessageSlot, "error");
        phoneField.requestFocus();
    }

    private void resetForm() {
        nameField.setText("");
        dobField.setText("");
        genderCombo.setSelectedIndex(0);
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
        clearErrors();
        statusBanner.clear();
    }

    private void handleSubmit() {
        statusBanner.clear();
        clearErrors();

        String name = nameField.getText().trim();
        String dobStr = dobField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty()) {
            showError(nameError, nameField, "Full Name is required.");
            return;
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr, DateTimeFormatter.ISO_LOCAL_DATE);
            if (dob.isAfter(LocalDate.now())) {
                showError(dobError, dobField, "Date of birth cannot be in the future.");
                return;
            }
        } catch (DateTimeParseException ex) {
            showError(dobError, dobField, "Invalid Date of Birth format. Please use YYYY-MM-DD (e.g. 1995-04-12).");
            return;
        }

        if (phone.isEmpty() || !phone.matches("^[0-9+ -]{7,15}$")) {
            showPhoneError("Please provide a valid phone number (digits only, 7-15 chars).");
            return;
        }

        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError(emailError, emailField, "Invalid email format.");
            return;
        }

        try {
            Patient newPatient = new Patient(0, name, dob, gender, phone,
                    email.isEmpty() ? null : email, address.isEmpty() ? null : address);
            int newId = DAOFactory.getInstance().getPatientDAO().registerPatient(newPatient);
            newPatient.setPatientId(newId);

            JOptionPane.showMessageDialog(this,
                    "Registration successful!\nYour Patient ID is: #" + newId + "\nRegistered Phone: " + phone,
                    "Registration Confirmed",
                    JOptionPane.INFORMATION_MESSAGE);

            if (callback != null) {
                callback.onRegistrationSuccess(newPatient);
            }
        } catch (Exception ex) {
            statusBanner.showMessage("Registration failed: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }
}
