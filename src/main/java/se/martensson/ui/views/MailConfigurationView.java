package se.martensson.ui.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.router.Route;

import se.martensson.entity.YoloObjectEntity;
import se.martensson.service.YoloObjectService;
import se.martensson.ui.views.templates.BarForAppLayout;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.TextRenderer;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.CheckBoxGroupProvider;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;

/**
 * The main view contains a button and a click listener.
 */
@Route("mailconfiguration")
@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
public class MailConfigurationView extends AppLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MailConfigurationView(YoloObjectService yoloObjectService) {
		// Banner and tabs
		BarForAppLayout barForApplayput = new BarForAppLayout();
		addToNavbar(barForApplayput.getDrawerToggle(), barForApplayput.getImg());
		addToDrawer(barForApplayput.getTabs());

		// CRUD instance
        GridCrud<YoloObjectEntity> crud = new GridCrud<>(YoloObjectEntity.class);

        // Grid configuration
        crud.getGrid().setColumns("email", "active", "objectName", "message", "messageHasBeenSent");
        crud.getGrid().setColumnReorderingAllowed(true);

        // CRUD form configuration
        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties("email", "active", "objectName", "message", "messageHasBeenSent");

        // CRUD logic configuration
        crud.setOperations(() -> yoloObjectService.findAll(),
                user -> yoloObjectService.save(user),
                user -> yoloObjectService.save(user),
                user -> yoloObjectService.delete(user)
        );
        
        // Layout configuration
        setContent(crud);
        crud.setFindAllOperationVisible(false);
	}
}
