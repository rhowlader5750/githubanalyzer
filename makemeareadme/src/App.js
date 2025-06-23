import './App.css';
import { useState } from 'react';
import { getallRepo, getReadMe, getUserInfo, saveRepo, deleteRepo, fetchSavedRepos } from './api/axios';
import UserInfo from './components/User/userInfo';
import Modal from './components/Repos/RepoModal';
import RepoList from './components/Repos/RepoList';




function App() {
  const [username, setUsername] = useState('');
  const [repos, setRepos] = useState([]);
  const [userInfo, setUserInfo] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [readMeContent, setReadMeContent] = useState('');
  const [activeTab, setActiveTab] = useState("all"); 
  const [savedRepos, setSavedRepos] = useState([]);
  const [isProfileModalOpen, setIsProfileModalOpen] = useState(false);
  const [profileReadme, setProfileReadme] = useState('');


  const reposPerPage = 30;
  const indexOfLastRepo = currentPage * reposPerPage;
  const indexOfFirstRepo = indexOfLastRepo - reposPerPage;
  const currentRepos = repos.slice(indexOfFirstRepo, indexOfLastRepo);



  const handleClick = async () => {
    try {
      const repoData = await getallRepo(username);
      const userInfo = await getUserInfo(username);
      const savedRepos = await fetchSavedRepos(username); 

      console.log('Fetched repos:', repoData);
      console.log('Fetched user info:', userInfo);

      setRepos(repoData.repos);
      setUserInfo(userInfo); 
      setSavedRepos(savedRepos);

    } catch (err) {
      console.error('Error fetching data:', err);
    }
  };

  const downloadReadme = (content, filename = "README.md") => {
    const blob = new Blob([content], { type: "text/markdown" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  };

 



  
 
  return (
    <div className="App" style={{ padding: '2rem' }}>
      <h1>Github Analyzer</h1>

      {/* Input Section */}
      <div style={{ marginBottom: '1.5rem' }}>
        <input
          type="text"
          className="form-control"
          placeholder="Enter GitHub username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          style={{ maxWidth: '300px', display: 'inline-block', marginRight: '10px' }}
        />
        <button type="submit" onClick={handleClick}>
          Submit
        </button>
      </div>

      {/* Main Layout Section */}
      <div style={{ display: 'flex', gap: '30px', alignItems: 'flex-start' }}>
        {/* Sidebar */}
        <div style={{ width: '300px', padding: '1rem', marginLeft: '7rem' }}>
          <UserInfo
            avatar_url={userInfo.avatar_url}
            name={userInfo.name}
            followers={userInfo.followers}
            html_url={userInfo.html_url}
            public_repos={userInfo.public_repos}
            bio={userInfo.bio}
            username = {username}
            savedRepos = {savedRepos}
            setProfileReadme={setProfileReadme}
            setIsProfileModalOpen={setIsProfileModalOpen}
          />
        </div>



        {/* Tabs + Repos column */}
  <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
    {/* Tabs */}
    <div style={{ display: 'flex', borderBottom: '2px solid #ddd', marginBottom: '1rem' }}>
      <div
        onClick={() => setActiveTab("all")}
        style={{
          padding: '0.75rem 10.35rem',
          cursor: 'pointer',
          borderBottom: activeTab === "all" ? '3px solid #007bff' : '3px solid transparent',
          fontWeight: activeTab === "all" ? 'bold' : 'normal',
          color: activeTab === "all" ? '#007bff' : '#333',
          backgroundColor: activeTab === "all" ? '#fff' : '#f7f7f7',
          borderTopLeftRadius: '5px',
          borderTopRightRadius: '5px'
        }}
      >
        All Repos
      </div>
      <div
        onClick={() => setActiveTab("saved")}
        style={{
          padding: '0.75rem 10.35rem',
          cursor: 'pointer',
          borderBottom: activeTab === "saved" ? '3px solid #007bff' : '3px solid transparent',
          fontWeight: activeTab === "saved" ? 'bold' : 'normal',
          color: activeTab === "saved" ? '#007bff' : '#333',
          backgroundColor: activeTab === "saved" ? '#fff' : '#f7f7f7',
          borderTopLeftRadius: '5px',
          borderTopRightRadius: '5px'
        }}
      >
        Saved Repos
      </div>
    </div>

    {/* Repos */}
    <div>
      <RepoList
        repos={activeTab === "all" ? currentRepos : savedRepos}
        username={username}
        onReadMeClick={getReadMe}
        setIsModalOpen={setIsModalOpen}
        setReadMeContent={setReadMeContent}
        toggleSave={(repo) => {
  const exists = savedRepos.some((r) => r.id === repo.id);

  if (exists) {
    // Remove from backend
    deleteRepo(repo.id)
      .then(() => {
        // Remove from frontend state
        setSavedRepos(savedRepos.filter((r) => r.id !== repo.id));
      })
      .catch((err) => {
        console.error("Error deleting repo:", err);
      });
  } else {
    // Add to backend
    saveRepo({
      id: repo.id,
      username: username,
      name: repo.name,
      description: repo.description,
      language: repo.language
    })
      .then((res) => {
        // Add to frontend state
        setSavedRepos([...savedRepos, res.data]);
      })
      .catch((err) => {
        console.error("Error saving repo:", err);
      });
  }
}}

      savedRepos={savedRepos}
      />
    </div>
  </div>
  </div>

      {isModalOpen && (
      <Modal handleCloseModal={() => setIsModalOpen(false)}>
        <h3>Generated README</h3>
        <hr style={{ margin: "1rem 0", border: "1px solid #ccc" }} />
        <pre style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
          {readMeContent}
        </pre>
        <button
                className="btn btn-success"
                onClick={() => downloadReadme(readMeContent)}
                style={{ marginTop: "1rem" }}
              >
                ⬇️ Download README
        </button>


      </Modal>
    )}
    {isProfileModalOpen && (
  <Modal handleCloseModal={() => setIsProfileModalOpen(false)}>
    <h3>Generated Profile README</h3>
    <hr style={{ margin: "1rem 0", border: "1px solid #ccc" }} />
    <pre style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
      {profileReadme}
    </pre>
    <button
      className="btn btn-success"
      onClick={() => downloadReadme(profileReadme, `${username}_PROFILE_README.md`)}
      style={{ marginTop: "1rem" }}
    >
      ⬇️ Download README
    </button>
  </Modal>
)}



      {/* Pagination */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          gap: '1rem',
          marginTop: '2rem',
        }}
      >
        <button
          onClick={() => setCurrentPage((prev) => prev - 1)}
          disabled={currentPage === 1}
          style={{
            padding: '0.5rem 1rem',
            backgroundColor: currentPage === 1 ? '#ccc' : '#007bff',
            color: '#fff',
            border: 'none',
            borderRadius: '5px',
            cursor: currentPage === 1 ? 'not-allowed' : 'pointer',
          }}
        >
          Prev
        </button>

        <p style={{ margin: 0, fontWeight: 'bold' }}>{currentPage}</p>

        <button
          onClick={() => setCurrentPage((prev) => prev + 1)}
          disabled={indexOfLastRepo >= repos.length}
          style={{
            padding: '0.5rem 1rem',
            backgroundColor: indexOfLastRepo >= repos.length ? '#ccc' : '#337bff',
            color: '#fff',
            border: 'none',
            borderRadius: '5px',
            cursor: indexOfLastRepo >= repos.length ? 'not-allowed' : 'pointer',
          }}
        >
          Next
        </button>
      </div>
    </div>



  );
}

export default App;
