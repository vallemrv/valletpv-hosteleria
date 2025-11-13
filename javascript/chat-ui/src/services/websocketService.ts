export class WebSocketService {
	private socket: WebSocket | null = null;

	constructor(private url: string) {}

	connect() {
		this.socket = new WebSocket(this.url);

		this.socket.onopen = () => {
			console.log('WebSocket conectado');
		};

		this.socket.onmessage = (event) => {
			const data = JSON.parse(event.data);
			// Aquí puedes emitir eventos o manejar los datos recibidos
			console.log('Mensaje recibido:', data);
		};

		this.socket.onclose = () => {
			console.log('WebSocket desconectado');
		};

		this.socket.onerror = (error) => {
			console.error('Error en WebSocket:', error);
		};
	}

	sendMessage(message: any) {
		if (this.socket && this.socket.readyState === WebSocket.OPEN) {
			this.socket.send(JSON.stringify(message));
		} else {
			console.error('WebSocket no está conectado');
		}
	}

	disconnect() {
		this.socket?.close();
	}
}
