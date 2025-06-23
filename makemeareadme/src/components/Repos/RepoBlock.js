import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faStar as solidStar } from '@fortawesome/free-solid-svg-icons';
import { faStar as regularStar } from '@fortawesome/free-regular-svg-icons';


export default function RepoBlock(props) {
  const { name, description, language, username, onReadMeClick, setIsModalOpen, setReadMeContent, toggleSave, isSaved } = props;
  
   const handleClick = () => {
    onReadMeClick(username, name)
      .then((data) => {
        console.log("README data:", data);
        setReadMeContent(data);
        setIsModalOpen(true);
      })
      .catch((err) => {
        console.error("Error fetching README:", err);
      });
  };
  

  return (
    <div
      className="card m-2"
      style={{
        width: "50rem",
        border: "1px solid #D3D3D3",
        borderRadius: "8px",
        padding: "1rem",
        margin: "3px",
        overflow: "hidden",
      }}
    >
  <div className="card-body" style={{ padding: 0 }}>
    {/* First Row: Repo name and button */}
    <div
      style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: "0.5rem",
      }}
    >
      <h3 className="card-title" style={{ margin: 0 }}>{name}</h3>
      <button
  onClick={toggleSave}
  style={{
    background: "none",
    border: "none",
    fontSize: "1.5rem",
    cursor: "pointer",
    color: isSaved ? "gold" : "gray",
  }}
  title={isSaved ? "Unsave Repo" : "Save Repo"}
>
  <FontAwesomeIcon icon={isSaved ? solidStar : regularStar} />
</button>

      <button
        className="btn btn-primary"
        style={{
          backgroundColor: "#4A90E2",
          borderColor: "#4A90E2",
          padding: "0.6rem 1.2rem",
          fontSize: "1rem",
          borderRadius: "8px",
          boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)",
          transition: "all 0.2s ease-in-out",
        }}
        onMouseOver={(e) => {
          e.target.style.backgroundColor = "#357ABD";
          e.target.style.borderColor = "#357ABD";
        }}
        onMouseOut={(e) => {
          e.target.style.backgroundColor = "#4A90E2";
          e.target.style.borderColor = "#4A90E2";
        }}
        onClick={handleClick}
      >
        🚀 Create Read Me
      </button>

    </div>

    {/* Second Row: Description and Language */}
    <div
      style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "flex-start",
      }}
    >
      <p
      className="card-text"
      style={{
        flex: 1,
        marginRight: "1rem",
        padding: "0.5rem",
        backgroundColor: "#f9f9f9",
        borderRadius: "6px",
        fontStyle: "italic",
        color: "#444",
      }}
    >
  {description}
</p>

      <h6 className="card-subtitle text-muted" style={{ whiteSpace: "nowrap" }}>
        {language}
      </h6>
    </div>
  </div>
</div>

  );

};