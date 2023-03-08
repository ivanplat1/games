package com.ivpl.games.view;

import com.ivpl.games.entity.jpa.User;
import com.ivpl.games.repository.UserRepository;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static com.ivpl.games.constants.Constants.PASSWORD_VALIDATION_EXCEPTION_MESSAGE;

@Route("registration")
@AnonymousAllowed
public class RegistrationView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(RegistrationView.class);

    private final transient UserRepository userRepository;
    private final transient BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationView(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        RegistrationForm registrationForm = new RegistrationForm();
        setHorizontalComponentAlignment(Alignment.CENTER, registrationForm);

        add(registrationForm);

        RegistrationFormBinder registrationFormBinder = new RegistrationFormBinder(registrationForm);
        registrationFormBinder.addBindingAndValidation();
    }

    public class RegistrationFormBinder {

        private final RegistrationForm registrationForm;

        /**
         * Flag for disabling first run for password validation
         */
        private boolean enablePasswordValidation;
        private boolean enableUsernameValidation;

        public RegistrationFormBinder(RegistrationForm registrationForm) {
            this.registrationForm = registrationForm;
        }

        /**
         * Method to add the data binding and validation logics to the registration form
         */
        public void addBindingAndValidation() {
            BeanValidationBinder<User> binder = new BeanValidationBinder<>(User.class);
            binder.bindInstanceFields(registrationForm);

            binder.forField(registrationForm.getPasswordField())
                    .withValidator(this::passwordValidator).bind("password");
            binder.forField(registrationForm.getUsernameField())
                    .withValidator(this::usernameValidator).bind("username");

            registrationForm.getPasswordConfirmField().addValueChangeListener(e -> {
                enablePasswordValidation = true;
                binder.validate();
            });

            binder.setStatusLabel(registrationForm.getErrorMessageField());

            registrationForm.getSubmitButton().addClickListener(event -> {
                try {
                    User user = new User();
                    binder.writeBean(user);
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    userRepository.saveAndFlush(user);
                    showSuccess(user);
                } catch (ValidationException e) {
                    log.error(PASSWORD_VALIDATION_EXCEPTION_MESSAGE, e.getMessage());
                }
            });
        }

        private ValidationResult passwordValidator(String pass1, ValueContext ctx) {
            if (!enablePasswordValidation) {
                enablePasswordValidation = true;
                return ValidationResult.ok();
            }
            String pass2 = registrationForm.getPasswordConfirmField().getValue();

            if (pass1 != null && pass1.equals(pass2))
                return ValidationResult.ok();

            return ValidationResult.error("Passwords do not match");
        }

        private ValidationResult usernameValidator(String username, ValueContext ctx) {
            if (!enableUsernameValidation) {
                enableUsernameValidation = true;
                return ValidationResult.ok();
            }

            if (userRepository.findByUsername(username).isEmpty())
                return ValidationResult.ok();

            return ValidationResult.error(String.format("User with name %s already exists.", username));
        }

        /**
         * We call this method when form submission has succeeded
         */
        private void showSuccess(User userBean) {
            Notification notification =
                    Notification.show("Data saved, welcome " + userBean.getUsername());
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
    }
}