import os

def replace_in_file(filepath):
    print(f"Processing {filepath}")
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Replaces for User forms
    replacements = [
        # ModifierUser.fxml
        (
            'style="-fx-background-color: #f7f9f4;"',
            'styleClass="user-form-root"'
        ),
        (
            '''style="-fx-font-size: 27px;
                                      -fx-font-weight: bold;
                                      -fx-text-fill: #1a3318;
                                      -fx-padding: 0 0 4 0;"''',
            '''styleClass="user-form-title"
                               style="-fx-font-size: 27px;
                                      -fx-font-weight: bold;
                                      -fx-padding: 0 0 4 0;"'''
        ),
        (
            '''style="-fx-font-size: 12px;
                                      -fx-text-fill: #7a9475;
                                      -fx-padding: 0 0 16 0;"''',
            '''styleClass="user-form-subtitle"
                               style="-fx-font-size: 12px;
                                      -fx-padding: 0 0 16 0;"'''
        ),
        (
            '''style="-fx-background-color: white;
                                     -fx-border-color: #c8dcc5;
                                     -fx-border-width: 1.5;
                                     -fx-border-radius: 12;
                                     -fx-background-radius: 12;
                                     -fx-padding: 10 12 10 12;"''',
            '''styleClass="user-form-card"
                              style="-fx-border-width: 1.5;
                                     -fx-border-radius: 12;
                                     -fx-background-radius: 12;
                                     -fx-padding: 10 12 10 12;"'''
        ),
        
        # Dashboard General
        (
            'style="-fx-background-color: #f0f4f0;"',
            'styleClass="user-form-root"'
        ),
        (
            '''style="-fx-background-color: white;
                              -fx-background-radius: 15;"''',
            '''styleClass="user-form-card"
                              style="-fx-background-radius: 15;"'''
        ),
        (
            '''style="-fx-font-size: 18px; -fx-font-weight: bold;"''',
            '''styleClass="user-form-title" style="-fx-font-size: 18px; -fx-font-weight: bold;"'''
        ),
        (
            '''style="-fx-text-fill: gray;"''',
            '''styleClass="user-form-subtitle"'''
        ),
        (
            '''style="-fx-background-color: white;
                                     -fx-padding: 20;
                                     -fx-background-radius: 15;"''',
            '''styleClass="user-form-card"
                                     style="-fx-padding: 20;
                                            -fx-background-radius: 15;"'''
        ),
        (
            '''style="-fx-font-size: 26px;
                                      -fx-font-weight: bold;
                                      -fx-text-fill: #228B22;"''',
            '''styleClass="user-form-title"
                               style="-fx-font-size: 26px;
                                      -fx-font-weight: bold;"'''
        ),
        (
            '''style="-fx-font-size: 13.5px;
                                      -fx-text-fill: #6b7280;"''',
            '''styleClass="user-form-subtitle"
                               style="-fx-font-size: 13.5px;"'''
        ),
        # MainGuiAdmin specifics
        (
            '''style="-fx-background-color: white;
                             -fx-background-radius: 16;
                             -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 3);"''',
            '''styleClass="user-form-card"
                             style="-fx-background-radius: 16;
                                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 3);"'''
        ),
        (
            '''style="-fx-font-size: 12px; -fx-text-fill: #6b7280;"''',
            '''styleClass="user-form-subtitle" style="-fx-font-size: 12px;"'''
        ),
        (
            '''style="-fx-background-color: white;
                                     -fx-padding: 20;
                                     -fx-background-radius: 15;"''',
            '''styleClass="user-form-card"
                                     style="-fx-padding: 20;
                                            -fx-background-radius: 15;"'''
        ),
        (
            '''style="-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;"''',
            '''styleClass="user-form-title" style="-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;"'''
        )
    ]

    for old_str, new_str in replacements:
        if old_str in content:
            content = content.replace(old_str, new_str)
            print(f"Replaced string in {filepath}")

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

if __name__ == "__main__":
    pwd = r"C:\integration pi\user_model\src\main\resources"
    replace_in_file(os.path.join(pwd, "ModifierUser.fxml"))
    replace_in_file(os.path.join(pwd, "Dashboard.fxml"))
    replace_in_file(os.path.join(pwd, "MainGuiAdmin.fxml"))
    replace_in_file(os.path.join(pwd, "MainGui.fxml"))
