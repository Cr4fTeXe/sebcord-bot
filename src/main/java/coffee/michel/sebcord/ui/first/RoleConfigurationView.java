
package coffee.michel.sebcord.ui.first;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.configuration.persistence.SebcordBot;
import coffee.michel.sebcord.configuration.persistence.SebcordBot.RoleTransition;
import coffee.michel.sebcord.configuration.persistence.SebcordBot.RoleTransition.RoleAction;
import coffee.michel.sebcord.ui.components.EditableGrid;

@Route(value = "roles", layout = ConfigurationMainContainer.class)
public class RoleConfigurationView extends VerticalLayout {
	private static final long				serialVersionUID	= 6152683733129390841L;

	private ConfigurationPersistenceManager	persistence			= new ConfigurationPersistenceManager();

	public RoleConfigurationView() {
		super();
		this.initUI();
	}

	private void initUI() {
		SebcordBot botConfig = persistence.getBotConfig();

		H3 muteRoleHeader = new H3("Mute Rolle");
		TextField muteRoleField = new TextField("Rollen ID");
		muteRoleField.setValue(botConfig.getMuteRoleId() + "");
		add(muteRoleHeader, muteRoleField);

		H3 initialRoleHeader = new H3("Initiale Rollen");
		HorizontalLayout initRoleLayout = new HorizontalLayout();
		TextField initRoleField = new TextField("Rollen ID");
		ListBox<Long> ids = new ListBox<>();
		ids.setItems(botConfig.getInitialRoles());
		Button addInitRoleButton = new Button("Hinzufügen");
		addInitRoleButton.addClickListener(ce -> {
			String value = initRoleField.getValue();
			if (value == null || value.isEmpty())
				return;
			Long roleId = Long.valueOf(value);
			botConfig.getInitialRoles().add(roleId);
			ids.setItems(botConfig.getInitialRoles());
		});
		Button removeInitRoleButton = new Button("Entfernen");
		removeInitRoleButton.addClickListener(ce -> ids.getOptionalValue().ifPresent(val -> {
			botConfig.getInitialRoles().remove(val);
			ids.setItems(botConfig.getInitialRoles());
		}));
		initRoleLayout.add(initRoleField, addInitRoleButton, removeInitRoleButton);
		add(initialRoleHeader, initRoleLayout, ids);

		H3 devUserHeader = new H3("Developer IDs");
		HorizontalLayout devUserLayout = new HorizontalLayout();
		TextField devUserField = new TextField("User ID");
		ListBox<Long> devIds = new ListBox<>();
		devIds.setItems(botConfig.getDeveloperIds());
		Button addDevUserButton = new Button("Hinzufügen");
		addDevUserButton.addClickListener(ce -> {
			String value = devUserField.getValue();
			if (value == null || value.isEmpty())
				return;
			Long roleId = Long.valueOf(value);
			botConfig.getDeveloperIds().add(roleId);
			devIds.setItems(botConfig.getDeveloperIds());
		});
		Button removeDevUserButton = new Button("Entfernen");
		removeDevUserButton.addClickListener(ce -> devIds.getOptionalValue().ifPresent(val -> {
			botConfig.getDeveloperIds().remove(val);
			devIds.setItems(botConfig.getDeveloperIds());
		}));
		devUserLayout.add(devUserField, addDevUserButton, removeDevUserButton);
		add(devUserHeader, devUserLayout, devIds);

		H3 roleTransitionHeader = new H3("Rollen Übergänge");

		EditableGrid<RoleTransition> grid = new EditableGrid<>(RoleTransition.class);
		grid.removeAllColumns();

		Binder<RoleTransition> trBinder = new Binder<>(RoleTransition.class);
		TextField triggerRoleField = new TextField();
		trBinder.bind(triggerRoleField, tr -> String.valueOf(tr.getTriggerAction().getRoleId()),
				(tr, vf) -> tr.getTriggerAction().setRoleId(Long.valueOf(vf)));
		Checkbox triggerRoleAdd = new Checkbox();
		trBinder.bind(triggerRoleAdd, "triggerAction.add");
		TextField actionToApplyRole = new TextField();
		trBinder.bind(actionToApplyRole, tr -> String.valueOf(tr.getActionToApply().getRoleId()),
				(tr, vf) -> tr.getActionToApply().setRoleId(Long.valueOf(vf)));
		Checkbox actionToApplyAdd = new Checkbox();
		trBinder.bind(actionToApplyAdd, "actionToApply.add");
		grid.getEditor().setBinder(trBinder);
		grid.addColumn("triggerAction.roleId").setHeader("Trigger-Rolle")
				.setEditorComponent(triggerRoleField);
		grid.addColumn("triggerAction.add").setHeader("Aktion").setEditorComponent(triggerRoleAdd);
		grid.addColumn("actionToApply.roleId").setHeader("Angewandte Rolle")
				.setEditorComponent(actionToApplyRole);
		grid.addColumn("actionToApply.add").setHeader("Aktion").setEditorComponent(actionToApplyAdd);
		grid.setDropMode(GridDropMode.BETWEEN);
		grid.addSelectionListener(sel -> sel.getFirstSelectedItem().ifPresent(grid.getEditor()::editItem));
		grid.getElement().addEventListener("keydown", event -> {
			Editor<RoleTransition> editor = grid.getEditor();
			editor.save();
			editor.closeEditor();
			grid.getDataProvider().refreshAll();
		}).setFilter("event.key === 'Enter'");
		grid.setRowsDraggable(true);

		AtomicReference<RoleTransition> draggedItem = new AtomicReference<>();
		grid.addDragStartListener(event -> {
			draggedItem.set(event.getDraggedItems().get(0));
			grid.setDropMode(GridDropMode.BETWEEN);
		});

		grid.addDragEndListener(event -> {
			draggedItem.set(null);
			grid.setDropMode(null);
		});

		grid.addDropListener(event -> {
			var dropOverItem = event.getDropTargetItem().get();
			if (!dropOverItem.equals(draggedItem.get())) {
				grid.removeItem(draggedItem.get());
				int dropIndex = grid.indexOfItem(dropOverItem)
						+ (event.getDropLocation() == GridDropLocation.BELOW ? 1
								: 0);
				grid.addItem(dropIndex, draggedItem.get());
				grid.getDataProvider().refreshAll();
			}
		});
		botConfig.getRoleTransitions().forEach(grid::addItem);

		Button addItemButton = new Button("Hinzufügen", ce -> grid.addItem(new RoleTransition()));
		Button removeItemButton = new Button("Entfernen", ce -> grid.getSelectedItems().forEach(grid::removeItem));

		add(roleTransitionHeader, grid, new HorizontalLayout(addItemButton, removeItemButton));

		Button saveButton = new Button("Speichern");
		saveButton.addClickListener(ce -> {
			botConfig.getInitialRoles().forEach(persistence::persist);
			List<RoleTransition> transitions = grid.getItems();
			botConfig.setRoleTransitions(transitions);
			botConfig.setMuteRoleId(Optional.ofNullable(muteRoleField.getValue()).filter(s -> !s.isEmpty())
					.map(Long::valueOf).orElse(0L));
			persistence.persist(botConfig);
			persistence.persist(botConfig, botConfig.getInitialRoles());
			persistence.persist(botConfig, botConfig.getRoleTransitions());
		});

		add(saveButton);
		this.setSizeFull();
	}

	static enum AddOrRemove {
		ADD("hinzugefügt", true),
		REMOVE("entfernt", false);

		private String	caption;
		private boolean	action;

		private AddOrRemove(String test, boolean action) {
			this.caption = test;
			this.action = action;
		}

		@Override
		public String toString() {
			return caption;
		}

	}

	static class TransitionEntry extends HorizontalLayout {
		private static final long		serialVersionUID	= -1076545191858676142L;
		private TextField				role1Id;
		private ComboBox<AddOrRemove>	role1AddOrRemove;
		private TextField				role2Id;
		private ComboBox<AddOrRemove>	role2AddOrRemove;

		public TransitionEntry() {
			this(new RoleTransition());
		}

		public TransitionEntry(RoleTransition rt) {
			add(new Label("Wenn die Rolle "));
			var triggerAction = rt.getTriggerAction();
			var actionToApply = rt.getActionToApply();

			role1Id = new TextField("", "4589353859");
			role1Id.setValue("" + triggerAction.getRoleId());
			add(role1Id);
			role1AddOrRemove = new ComboBox<>("", AddOrRemove.ADD, AddOrRemove.REMOVE);
			role1AddOrRemove.setValue(triggerAction.isAdd() ? AddOrRemove.ADD : AddOrRemove.REMOVE);
			role1AddOrRemove.setRenderer(new TextRenderer<>(bean -> bean.caption));
			add(role1AddOrRemove);
			add(new Label("wird, wird die Rolle "));
			role2Id = new TextField("", "4589353859");
			role2Id.setValue("" + actionToApply.getRoleId());
			add(role2Id);
			role2AddOrRemove = new ComboBox<>("", AddOrRemove.ADD, AddOrRemove.REMOVE);
			role2AddOrRemove.setValue(actionToApply.isAdd() ? AddOrRemove.ADD : AddOrRemove.REMOVE);
			role2AddOrRemove.setRenderer(new TextRenderer<>(bean -> bean.caption));
			add(role2AddOrRemove);

			Button button = new Button(VaadinIcon.MINUS.create());
			button.addClickListener(ce -> getParent().ifPresent(p -> ((HasComponents) p).remove(this)));
			add(button);
		}

		RoleTransition get() {
			String value = role1Id.getValue();
			if (value == null || value.isEmpty())
				return null;
			long role1Id = Long.valueOf(value);

			AddOrRemove value2 = this.role1AddOrRemove.getValue();
			if (value2 == null)
				return null;
			boolean role1AddOrRemove = value2.action;

			value = role2Id.getValue();
			if (value == null || value.isEmpty())
				return null;
			long role2Id = Long.valueOf(value);

			value2 = this.role2AddOrRemove.getValue();
			if (value2 == null)
				return null;
			boolean role2AddOrRemove = value2.action;

			RoleTransition roleTransition = new RoleTransition();
			RoleAction roleAction = new RoleAction();
			roleAction.setAdd(role1AddOrRemove);
			roleAction.setRoleId(role1Id);
			roleTransition.setTriggerAction(roleAction);
			roleAction = new RoleAction();
			roleAction.setAdd(role2AddOrRemove);
			roleAction.setRoleId(role2Id);
			roleTransition.setActionToApply(roleAction);
			return roleTransition;
		}

	}

}
