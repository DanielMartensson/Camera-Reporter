package se.martenssonborg.ui.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.router.Route;

import se.martenssonborg.entity.ObjectNameEntity;
import se.martenssonborg.entity.YoloObjectEntity;
import se.martenssonborg.service.ObjectNameService;
import se.martenssonborg.service.YoloObjectService;

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

	public ConfigurationView(YoloObjectService yoloObjectService, ObjectNameService objectNameService) {
		
		System.out.println(yoloObjectService == null);
		System.out.println(objectNameService == null);
		// crud instance
        GridCrud<YoloObjectEntity> crud = new GridCrud<>(YoloObjectEntity.class);

        // grid configuration
        crud.getGrid().setColumns("email", "active", "objectName", "threshold");
        crud.getGrid().setColumnReorderingAllowed(true);

        // form configuration
        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties("email", "active", "threshold", "objectName");
        crud.getCrudFormFactory().setFieldProvider("objectName", new ComboBoxProvider<>("Object Name", objectNameService.findAll(), new TextRenderer<>(ObjectNameEntity::getName), ObjectNameEntity::getName));

        // layout configuration
        setContent(crud);
        crud.setFindAllOperationVisible(false);

        // logic configuration
        crud.setOperations(() -> yoloObjectService.findAll(),
                user -> yoloObjectService.save(user),
                user -> yoloObjectService.save(user),
                user -> yoloObjectService.delete(user)
        );
	}
}
