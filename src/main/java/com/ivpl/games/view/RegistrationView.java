package com.ivpl.games.view;

import com.ivpl.games.entity.User;
import com.ivpl.games.repository.UserRepository;
import com.ivpl.games.security.JpaUserDetailsManager;
import com.ivpl.games.security.SecurityConfiguration;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Route("registration")
@AnonymousAllowed
public class RegistrationView extends VerticalLayout {

    private SecurityConfiguration securityConfiguration;
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationView(SecurityConfiguration securityConfiguration, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.securityConfiguration = securityConfiguration;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        RegistrationForm registrationForm = new RegistrationForm();
        setHorizontalComponentAlignment(Alignment.CENTER, registrationForm);

        add(registrationForm);

        RegistrationFormBinder registrationFormBinder = new RegistrationFormBinder(registrationForm);
        registrationFormBinder.addBindingAndValidation();
    }

    public class RegistrationFormBinder {

        private RegistrationForm registrationForm;

        /**
         * Flag for disabling first run for password validation
         */
        private boolean enablePasswordValidation;

        public RegistrationFormBinder(RegistrationForm registrationForm) {
            this.registrationForm = registrationForm;
        }

        /**
         * Method to add the data binding and validation logics
         * to the registration form
         */
        public void addBindingAndValidation() {
            BeanValidationBinder<User> binder = new BeanValidationBinder<>(User.class);
            binder.bindInstanceFields(registrationForm);

            // A custom validator for password fields
            binder.forField(registrationForm.getPasswordField())
                    .withValidator(this::passwordValidator).bind("password");

            // The second password field is not connected to the Binder, but we
            // want the binder to re-check the password validator when the field
            // value changes. The easiest way is just to do that manually.
            registrationForm.getPasswordConfirmField().addValueChangeListener(e -> {
                // The user has modified the second field, now we can validate and show errors.
                // See passwordValidator() for how this flag is used.
                enablePasswordValidation = true;

                binder.validate();
            });

            // Set the label where bean-level error messages go
            binder.setStatusLabel(registrationForm.getErrorMessageField());

            // And finally the submit button
            registrationForm.getSubmitButton().addClickListener(event -> {
                try {
                    // Create empty bean to store the details into
                    User user = new User();

                    // Run validators and write the values to the bean
                    binder.writeBean(user);
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    userRepository.save(user);

                    // Typically, you would here call backend to store the bean

                    // Show success message if everything went well
                    showSuccess(user);
                } catch (ValidationException exception) {
                    // validation errors are already visible for each field,
                    // and bean-level errors are shown in the status label.
                    // We could show additional messages here if we want, do logging, etc.
                }
            });
        }

        /**
         * Method to validate that:
         * <p>
         * 1) Password is at least 8 characters long
         * <p>
         * 2) Values in both fields match each other
         */
        private ValidationResult passwordValidator(String pass1, ValueContext ctx) {
            /*
             * Just a simple length check. A real version should check for password
             * complexity as well!
             */


            if (!enablePasswordValidation) {
                // user hasn't visited the field yet, so don't validate just yet, but next time.
                enablePasswordValidation = true;
                return ValidationResult.ok();
            }

            String pass2 = registrationForm.getPasswordConfirmField().getValue();

            if (pass1 != null && pass1.equals(pass2)) {
                return ValidationResult.ok();
            }

            return ValidationResult.error("Passwords do not match");
        }

        /**
         * We call this method when form submission has succeeded
         */
        private void showSuccess(User userBean) {
            Notification notification =
                    Notification.show("Data saved, welcome " + userBean.getUsername());
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Here you'd typically redirect the user to another view
        }
    }
}