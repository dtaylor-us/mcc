import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useApp } from '../App';
import { agentService } from '../services/apiService';
import { ChatMessage } from '../types';
import { PaperAirplaneIcon } from './icons';

type AgentChatDockProps = {
  id?: string;
  title?: string;
  className?: string;
  isOpen: boolean;          // controlled by parent
  onClose: () => void;      // close handler
  onUnreadChange?: (n: number) => void; // badge in header
};

function normalizeAgentResponse(resp: any): { text: string; toolCount?: number; toolCalls?: unknown[] } {
  if (resp?.content?.answer) return { text: resp.content.answer, toolCount: resp?.toolCount, toolCalls: resp?.toolCalls };
  if (resp?.answer) return { text: resp.answer, toolCount: resp.toolCount };
  if (typeof resp === 'string') return { text: resp };
  return { text: JSON.stringify(resp) };
}

const AgentChatDock: React.FC<AgentChatDockProps> = ({
  id = 'agent-chat-dock',
  title = 'Ops Agent',
  className,
  isOpen,
  onClose,
  onUnreadChange,
}) => {
  const { conversationId } = useApp();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [isThinking, setIsThinking] = useState(false);
  const [unreadLocal, setUnreadLocal] = useState(0);

  const panelRef = useRef<HTMLDivElement>(null);
  const scrollRef = useRef<HTMLDivElement>(null);

  // reset unread when opened; notify parent
  useEffect(() => {
    if (isOpen && unreadLocal > 0) {
      setUnreadLocal(0);
      onUnreadChange?.(0);
      requestAnimationFrame(() => {
        scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'smooth' });
      });
    }
  }, [isOpen, unreadLocal, onUnreadChange]);

  // auto-scroll if open; count unread if closed and assistant speaks
  useEffect(() => {
    if (isOpen) {
      scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'smooth' });
    } else if (messages.length > 0) {
      const last = messages[messages.length - 1];
      if (last?.from === 'assistant') {
        setUnreadLocal(u => {
          const next = u + 1;
          onUnreadChange?.(next);
          return next;
        });
      }
    }
  }, [messages, isOpen, onUnreadChange]);

  // click outside to close
  useEffect(() => {
    const onDocClick = (e: MouseEvent) => {
      if (!isOpen) return;
      const t = e.target as Node;
      if (panelRef.current && !panelRef.current.contains(t)) onClose();
    };
    document.addEventListener('mousedown', onDocClick);
    return () => document.removeEventListener('mousedown', onDocClick);
  }, [isOpen, onClose]);

  // ESC to close
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isOpen) onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [isOpen, onClose]);

  const send = useCallback(async () => {
    const trimmed = input.trim();
    if (!trimmed || isThinking) return;

    const userMessage: ChatMessage = { from: 'user', text: trimmed };
    setMessages(prev => [...prev, userMessage]);
    setInput('');
    setIsThinking(true);

    try {
      const resp = await agentService.askAgent(trimmed, conversationId);
      const { text, toolCount, toolCalls } = normalizeAgentResponse(resp);
      const assistantMessage: ChatMessage = { from: 'assistant', text, toolCount, toolCalls };
      setMessages(prev => [...prev, assistantMessage]);
    } catch (err: any) {
      const friendly = err?.message ?? 'Sorry, I encountered an error.';
      setMessages(prev => [...prev, { from: 'assistant', text: friendly }]);
    } finally {
      setIsThinking(false);
    }
  }, [input, isThinking, conversationId]);

  const onInputKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !isThinking) {
      e.preventDefault();
      send();
    }
  };

  return (
    <div
      id={id}
      className={`fixed right-4 bottom-4 z-50 pointer-events-none ${className ?? ''}`}
      aria-live="polite"
      aria-relevant="additions"
    >
      {/* Only render dialog when open so nothing can block clicks when closed */}
      {isOpen && (
        <div
          ref={panelRef}
          className="pointer-events-auto transition-all duration-200 ease-out opacity-100 translate-y-0"
          role="dialog"
          aria-modal="true"
          aria-label="Ops Agent chat"
        >
          <div className="w-[92vw] sm:w-[24rem] md:w-[28rem] h-[28rem] sm:h-[32rem] bg-slate-800 rounded-2xl shadow-2xl border border-slate-700 flex flex-col overflow-hidden">
            {/* Header */}
            <div className="flex items-center justify-between px-4 py-3 border-b border-slate-700">
              <div className="text-white font-semibold">{title}</div>
              <button
                onClick={onClose}
                className="text-slate-300 hover:text-white rounded-md px-2 py-1 focus:outline-none focus:ring-2 focus:ring-cyan-400"
                aria-label="Minimize chat"
                title="Minimize"
              >
                ▾
              </button>
            </div>

            {/* Messages */}
            <div ref={scrollRef} className="flex-1 p-4 space-y-3 overflow-y-auto">
              {messages.map((msg, i) => (
                <div key={i} className={`flex ${msg.from === 'user' ? 'justify-end' : 'justify-start'}`}>
                  <div
                    className={`max-w-[85%] rounded-2xl px-4 py-2 whitespace-pre-wrap break-words ${
                      msg.from === 'user' ? 'bg-cyan-700 text-white' : 'bg-slate-700 text-slate-100'
                    }`}
                  >
                    {msg.text}
                    {msg.from === 'assistant' && (msg.toolCount ?? (msg.toolCalls as any)?.length) ? (
                      <div className="mt-2 text-[11px] text-slate-300">
                        {typeof msg.toolCount === 'number'
                          ? `Tools invoked: ${msg.toolCount}`
                          : `Tools invoked: ${(msg.toolCalls as any)?.length ?? 0}`}
                      </div>
                    ) : null}
                  </div>
                </div>
              ))}
              {isThinking && <div className="text-slate-400 text-sm">Agent is thinking...</div>}
            </div>

            {/* Composer */}
            <div className="p-3 border-t border-slate-700 flex gap-2">
              <input
                type="text"
                value={input}
                onChange={e => setInput(e.target.value)}
                onKeyDown={onInputKeyDown}
                placeholder="Type a message…"
                className="flex-1 bg-slate-700 p-2 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-cyan-600"
                disabled={isThinking}
              />
              <button
                onClick={send}
                disabled={isThinking || !input.trim()}
                className="bg-cyan-600 hover:bg-cyan-500 text-white p-2 rounded-lg disabled:bg-slate-600"
                aria-label="Send"
                title="Send"
              >
                <PaperAirplaneIcon className="h-5 w-5" />
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AgentChatDock;
