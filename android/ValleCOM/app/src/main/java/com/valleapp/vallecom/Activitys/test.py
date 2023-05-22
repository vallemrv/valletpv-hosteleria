import tkinter as tk

def abrir_ventana():
    # Crea una nueva ventana
    nueva_ventana = tk.Tk()
    nueva_ventana.geometry('600x500')

    # Establece el título de la nueva ventana
    nueva_ventana.title('Adiós muy buenas')

    # Crea una etiqueta con el texto "Adiós muy buenas"
    etiqueta = tk.Label(nueva_ventana, text="Adiós muy buenas")

    # Coloca la etiqueta en la ventana
    etiqueta.pack()

# Crea una nueva ventana
ventana = tk.Tk()
ventana.geometry('600x500')

# Establece el título de la ventana
ventana.title('Hola Mundo')

# Crea un botón que abrirá una nueva ventana cuando se presione
boton = tk.Button(ventana, text="Abrir nueva ventana", command=abrir_ventana)

# Coloca el botón en la ventana
boton.pack()

# Inicia el bucle principal de la ventana
ventana.mainloop()
