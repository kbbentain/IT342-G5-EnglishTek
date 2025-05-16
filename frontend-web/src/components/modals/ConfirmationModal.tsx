import { AlertTriangle } from 'lucide-react';

interface ConfirmationModalProps {
  isOpen: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  onConfirm: () => void;
  onCancel: () => void;
  isDestructive?: boolean;
}

const ConfirmationModal = ({
  isOpen,
  title,
  message,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  onConfirm,
  onCancel,
  isDestructive = false,
}: ConfirmationModalProps) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      {/* Backdrop */}
      <div className="fixed inset-0 bg-black/50 transition-opacity" onClick={onCancel} />

      {/* Modal */}
      <div className="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
        <div className="relative transform overflow-hidden rounded-lg bg-base-100 text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg">
          <div className="p-6">
            {/* Header */}
            <div className="flex items-center gap-4 mb-4">
              <div className={`p-3 rounded-full ${isDestructive ? 'bg-error/10' : 'bg-warning/10'}`}>
                <AlertTriangle className={`w-6 h-6 ${isDestructive ? 'text-error' : 'text-warning'}`} />
              </div>
              <h3 className="text-lg font-semibold">{title}</h3>
            </div>

            {/* Message */}
            <p className="text-base-content/70 mb-6">
              {message}
            </p>

            {/* Actions */}
            <div className="flex justify-end gap-3">
              <button
                className="btn btn-ghost"
                onClick={onCancel}
              >
                {cancelLabel}
              </button>
              <button
                className={`btn ${isDestructive ? 'btn-error' : 'btn-primary'}`}
                onClick={onConfirm}
              >
                {confirmLabel}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ConfirmationModal;
