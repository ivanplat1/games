package com.ivpl.games.view;

import com.ivpl.games.components.EmployeeEditor;
import com.ivpl.games.entity.Employee;
import com.ivpl.games.repository.EmployeeRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;


public class MainView extends VerticalLayout {

    private final EmployeeRepository employeeRepository;

    private Grid<Employee> grid = new Grid<>(Employee.class);
    private final TextField filter = new TextField("", "Type to filter");
    private final Button addNewBtn = new Button("Add New");
    private final EmployeeEditor editor;
    private final HorizontalLayout toolbar = new HorizontalLayout(filter, addNewBtn);


    public MainView(EmployeeRepository employeeRepository, EmployeeEditor editor) {
        this.employeeRepository = employeeRepository;
        this.editor = editor;
        add(toolbar, grid, editor);

        // Replace listing with filtered content when user changes filter
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> showEmployee(e.getValue()));

        // Connect selected Customer to editor or hide if none is selected
        grid.asSingleSelect().addValueChangeListener(e -> editor.editEmployee(e.getValue()));

        // Instantiate and edit new Customer the new button is clicked
        addNewBtn.addClickListener(e -> editor.editEmployee(new Employee()));

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            showEmployee(filter.getValue());
        });

        showEmployee("");
    }

    private void showEmployee(String name) {
        if (!name.isEmpty()) {
            grid.setItems(employeeRepository.findByName(name));
        } else {
            grid.setItems(employeeRepository.findAll());
        }
    }
}
