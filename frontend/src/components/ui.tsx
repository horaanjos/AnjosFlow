import { useEffect, useRef, type ReactNode } from "react";
import { Inbox, LoaderCircle, X, ArrowRight } from "lucide-react";
import { Link } from "react-router-dom";
export function Loading() {
  return (
    <div className="state" role="status">
      <LoaderCircle className="spin" size={24} />
      Carregando seu espaço…
    </div>
  );
}
export function ErrorState({
  message,
  retry,
}: {
  message: string;
  retry?: () => void;
}) {
  return (
    <div className="alert" role="alert">
      <span>{message}</span>
      {retry && (
        <button className="button secondary" onClick={retry}>
          Tentar novamente
        </button>
      )}
    </div>
  );
}
export function Empty({
  title = "Um novo começo",
  text,
  action,
}: {
  title?: string;
  text: string;
  action?: ReactNode;
}) {
  return (
    <div className="state empty">
      <span className="empty-icon">
        <Inbox size={28} />
      </span>
      <h3>{title}</h3>
      <p>{text}</p>
      {action}
    </div>
  );
}
export function PageTitle({
  eyebrow = "SEU ESPAÇO DE ESTUDOS",
  title,
  description,
  action,
}: {
  eyebrow?: string;
  title: string;
  description: string;
  action?: ReactNode;
}) {
  return (
    <div className="page-title">
      <div>
        <span className="eyebrow">{eyebrow}</span>
        <h1>{title}</h1>
        <p>{description}</p>
      </div>
      {action}
    </div>
  );
}
export function Modal({
  title,
  children,
  onClose,
}: {
  title: string;
  children: ReactNode;
  onClose: () => void;
}) {
  const ref = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const dialog = ref.current;
    const previous = document.activeElement as HTMLElement | null;
    dialog?.showModal();
    return () => {
      dialog?.close();
      previous?.focus();
    };
  }, []);
  return (
    <dialog
      ref={ref}
      className="modal"
      aria-labelledby="modal-title"
      onCancel={(e) => {
        e.preventDefault();
        onClose();
      }}
      onClick={(e) => {
        if (e.target === ref.current) onClose();
      }}
    >
      <div className="modal-header">
        <h2 id="modal-title">{title}</h2>
        <button
          type="button"
          className="icon-button"
          aria-label="Fechar"
          onClick={onClose}
        >
          <X size={20} />
        </button>
      </div>
      {children}
    </dialog>
  );
}
export function Badge({
  children,
  tone = "neutral",
}: {
  children: ReactNode;
  tone?: "neutral" | "green" | "orange";
}) {
  return <span className={`badge ${tone}`}>{children}</span>;
}
export function SectionLink({
  to,
  children,
}: {
  to: string;
  children: ReactNode;
}) {
  return (
    <Link className="text-link" to={to}>
      {children}
      <ArrowRight size={15} />
    </Link>
  );
}
