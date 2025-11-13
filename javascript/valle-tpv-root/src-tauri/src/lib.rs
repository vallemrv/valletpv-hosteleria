#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
  tauri::Builder::default()
    .setup(|app| {
      if cfg!(debug_assertions) {
        app.handle().plugin(
          tauri_plugin_log::Builder::default()
            .level(log::LevelFilter::Info)
            .build(),
        )?;
      }
      Ok(())
    })
    .invoke_handler(tauri::generate_handler![saludar])
    .run(tauri::generate_context!())
    .expect("error while running tauri application");
}

#[tauri::command]
fn saludar(nombre: &str) -> String {
  format!("¡Hola {}! Saludos desde Rust 🦀", nombre)
}
