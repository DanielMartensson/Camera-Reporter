package se.martenssonborg.ui.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.router.Route;

import se.martenssonborg.entity.Group;
import se.martenssonborg.entity.User;
import se.martenssonborg.service.GroupService;
import se.martenssonborg.service.UserService;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.CheckBoxGroupProvider;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;

/**
 * The main view contains a button and a click listener.
 */
@Route("configuration")
@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
public class ConfigurationView extends AppLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConfigurationView(UserService userService, GroupService groupService) {
		// crud instance
        GridCrud<User> crud = new GridCrud<>(User.class);

        // grid configuration
        crud.getGrid().setColumns("name", "birthDate", "maritalStatus", "email", "phoneNumber", "active");
        crud.getGrid().setColumnReorderingAllowed(true);

        // form configuration
        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties("name", "birthDate", "email", "salary", "phoneNumber", "maritalStatus", "groups", "active", "mainGroup");
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "name", "birthDate", "email", "salary", "phoneNumber", "maritalStatus", "groups", "active", "mainGroup", "password");
        crud.getCrudFormFactory().setFieldProvider("mainGroup", new ComboBoxProvider<>(groupService.findAll()));
        crud.getCrudFormFactory().setFieldProvider("groups", new CheckBoxGroupProvider<>(groupService.findAll()));
        crud.getCrudFormFactory().setFieldProvider("groups", new CheckBoxGroupProvider<>("Groups", groupService.findAll(), Group::getName));
        crud.getCrudFormFactory().setFieldProvider("mainGroup", new ComboBoxProvider<>("Main Group", groupService.findAll(), new TextRenderer<>(Group::getName), Group::getName));

        // layout configuration
        setContent(crud);
        crud.setFindAllOperationVisible(false);

        // logic configuration
        crud.setOperations(() -> userService.findAll(),
                user -> userService.save(user),
                user -> userService.save(user),
                user -> userService.delete(user)
        );
	}
}
