import ReactDom from 'react-dom';
import './RepoModal.css';

export default function Modal({ children, handleCloseModal }) {
  return ReactDom.createPortal(
    <div className='modal-container'>
      {/* Removed the outside button that closes the modal */}
      <div className='modal-content'>
        {/* Close button inside the modal */}
        <button
          onClick={handleCloseModal}
          style={{
            float: 'right',
            background: 'transparent',
            border: 'none',
            fontSize: '1.5rem',
            cursor: 'pointer',
          }}
        >
          &times;
        </button>

        {children}
      </div>
    </div>,
    document.getElementById('portal')
  );
}
