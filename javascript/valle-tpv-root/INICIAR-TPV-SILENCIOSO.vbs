REM ==================================================
REM   Valle TPV - Inicio Silencioso (Sin consola)
REM ==================================================
Set objShell = CreateObject("WScript.Shell")
strPath = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)
objShell.CurrentDirectory = strPath
objShell.Run chr(34) & strPath & "\INICIAR-TPV.bat" & chr(34), 0, False
Set objShell = Nothing
