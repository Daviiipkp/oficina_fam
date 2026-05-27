import { useState, useEffect } from 'react'
import './App.css'

// URL do seu Back-end SSE
const SSE_URL = "https://oficina.daviipkp.org/events";

function App() {
  // Estado para armazenar a lista de usuários vindos do SSE
  const [users, setUsers] = useState([])
  // Estado para verificar a conexão (opcional, mas bom para o visual)
  const [status, setStatus] = useState('connecting')

  useEffect(() => {
    // 1. Cria a conexão SSE
    const eventSource = new EventSource(SSE_URL);

    // Ouve o evento genérico de abertura
    eventSource.onopen = () => {
      console.log("► Conexão SSE Estabelecida com a Oficina.");
      setStatus('online');
    };

    // Ouve o evento genérico de erro
    eventSource.onerror = (error) => {
      console.error("X Erro na conexão SSE:", error);
      setStatus('error');
      // O navegador tentará reconectar automaticamente
    };

    // 2. Ouve especificamente o evento "users" solicitado
    eventSource.addEventListener("users", (event) => {
      try {
        // O back-end envia uma Collection, que chega como string JSON
        const userDataArray = JSON.parse(event.data);
        console.log("↓ Usuários recebidos:", userDataArray);
        setUsers(userDataArray);
      } catch (parseError) {
        console.error("X Erro ao processar JSON de usuários:", parseError, event.data);
      }
    });

    // 3. Cleanup: Fecha a conexão quando o componente for desmontado
    return () => {
      console.log("■ Fechando conexão SSE.");
      eventSource.close();
    };
  }, []); // Executa apenas uma vez na montagem

  return (
    // Container Principal: Estilo Dark/Terminal
    <div className="min-h-screen bg-gray-950 text-green-400 font-mono p-4 md:p-8">
      
      {/* Header Gamificado */}
      <header className="border-b-2 border-green-800 pb-4 mb-8 flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-3xl font-bold tracking-widest text-green-100">
            &gt; OFICINA_DAVIIPKP // SISTEMA DE USUÁRIOS
          </h1>
          <p className="text-gray-500 mt-1">Status: <span className={`font-bold ${status === 'online' ? 'text-green-500' : 'text-red-500'}`}>{status.toUpperCase()}</span></p>
        </div>
        <div className="text-right border border-green-800 p-2 rounded">
          <div className="text-xs text-gray-500">PLAYERS_ONLINE</div>
          <div className="text-4xl font-extrabold text-green-100">{users.length}</div>
        </div>
      </header>

      {/* Conteúdo Principal */}
      <main>
        <section>
          <h2 className="text-xl mb-6 text-green-200 border-l-4 border-green-500 pl-3">
            Lista de Jogadores Recrutados
          </h2>

          {/* Grid de Usuários ou Tela Vazia */}
          {users.length === 0 ? (
            <div className="border-2 border-dashed border-gray-700 rounded-lg p-12 text-center text-gray-600">
              <p className="text-5xl mb-4">∅</p>
              <p>Aguardando dados do servidor...</p>
              <p className="text-xs mt-2">Verifique se o backend está rodando em {SSE_URL}</p>
            </div>
          ) : (
            // Grid Responsivo
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {users.map((username, index) => (
                // Card de Usuário Gamificado
                <div 
                  key={index} // Usando index pois recebemos apenas Strings simples
                  className="bg-gray-900 border border-green-900 rounded-md p-4 hover:border-green-500 hover:bg-black transition-all group shadow-md hover:shadow-green-900/50"
                >
                  <div className="flex items-center gap-3">
                    {/* "Avatar" simples */}
                    <div className="w-10 h-10 rounded-full bg-green-950 border border-green-800 flex items-center justify-center text-green-300 font-bold group-hover:bg-green-500 group-hover:text-black">
                      {username.substring(0, 2).toUpperCase()}
                    </div>
                    {/* Nome do Usuário */}
                    <div className="flex-1 overflow-hidden">
                      <p className="text-xs text-gray-600">NICKNAME</p>
                      <p className="text-lg font-semibold text-green-100 truncate group-hover:text-green-400">
                        {username}
                      </p>
                    </div>
                  </div>
                  {/* Detalhe estético inferior */}
                  <div className="mt-3 text-right text-xs text-green-900 group-hover:text-green-700">
                    ID: {index.toString().padStart(4, '0')}
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </main>

      {/* Footer Simples */}
      <footer className="mt-12 pt-4 border-t border-green-900 text-center text-xs text-gray-700">
        <p>&gt; HMR enabled. Edit src/App.jsx to test changes.</p>
        <p>Vite + React + Tailwind + Javalin SSE</p>
      </footer>
    </div>
  )
}

export default App