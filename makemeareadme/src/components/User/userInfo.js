
import { getProfileReadMe } from '../../api/axios';

export default function UserInfo(props){
  const {name, followers,  bio, language, html_url, public_repos, avatar_url, username, savedRepos, setProfileReadme, setIsProfileModalOpen} = props;
  
  const handleGenerateProfileReadme = async () => {
    if(!savedRepos || savedRepos.length === 0){
      alert("No saved repos");
      return;
    }
  try {
    const data = await getProfileReadMe(username); 
    setProfileReadme(data);
    setIsProfileModalOpen(true);
  } catch (err) {
    console.error('Error fetching profile README:', err);
    alert("Failed to generate profile README");
  }
};

  
  
  

  return (
    <div>
      <img
        src={avatar_url}
        alt={`${name}'s avatar`}
        style={{ width: '300px', height: '300px', borderRadius: '50%' }}
      />
      <h2>{name}</h2>
      {bio && <p>Bio: {bio}</p>}
      {language && <h6>Language: {language}</h6>}
      <h6>Follower Count: {followers}</h6>
      <h6>Github Link: <a href={html_url}>🐱 GitHub Link 🐱</a> </h6>
      <h6>Public Repos: {public_repos}</h6>
    
    
      <button onClick={handleGenerateProfileReadme}>Generate Profile README</button>
    </div>

    
    
  )
}

