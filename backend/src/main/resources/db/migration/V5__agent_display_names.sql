-- The two pilot agents are shown as "Agent 1" and "Agent 2".
-- (On a brand-new database the accounts are created with these names already;
--  this script renames them on databases created before the change.)

UPDATE app_user SET full_name = 'Agent 1' WHERE username = 'agent1';
UPDATE app_user SET full_name = 'Agent 2' WHERE username = 'agent2';
