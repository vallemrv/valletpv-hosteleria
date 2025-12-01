import qrcode
from io import BytesIO
import base64

def generate_qr_base64(data_string):
    """
    Genera una imagen QR a partir de una cadena de texto y la devuelve
    codificada en base64 lista para incrustar en HTML.
    """
    if not data_string:
        return ""
        
    qr = qrcode.QRCode(
        version=1,
        error_correction=qrcode.constants.ERROR_CORRECT_L,
        box_size=10,
        border=4,
    )
    qr.add_data(data_string)
    qr.make(fit=True)

    img = qr.make_image(fill_color="black", back_color="white")
    
    buffer = BytesIO()
    img.save(buffer, format="PNG")
    img_str = base64.b64encode(buffer.getvalue()).decode("utf-8")
    
    return f"data:image/png;base64,{img_str}"
