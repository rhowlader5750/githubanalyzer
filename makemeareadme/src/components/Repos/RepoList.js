import RepoBlock from './RepoBlock'; 

export default function RepoList({ repos, username, onReadMeClick, setIsModalOpen, setReadMeContent, toggleSave, savedRepos = [] }) {
  return (
    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', justifyContent: 'flex-start' }}>
      {repos.map((repo) => (
        <RepoBlock
          key={repo.id}
          username={username}
          name={repo.name}
          description={repo.description}
          language={repo.language}
          onReadMeClick={onReadMeClick}
          setIsModalOpen={setIsModalOpen}
          setReadMeContent={setReadMeContent}
          toggleSave = {() => toggleSave(repo)}
          isSaved={savedRepos.some(saved => saved.id === repo.id)}

        />
      ))}
    </div>
  );
}
