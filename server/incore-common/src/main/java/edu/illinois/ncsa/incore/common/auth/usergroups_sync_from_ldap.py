# pip install python-ldap - used 3.4.0
# pip install pymongo

# Must be on NCSA VPN to access the LDAP server URL

# TODO: Move this script to somewhere more appropriate - may be a new "scripts" folder in the root
# or to dev-scripts repo altogether

import ldap
import re
from pymongo import MongoClient

# TODO: Add auth as needed for prod and dev envs. Hardcode can be replaced with SPACE_MONGODB_URI
mongo_client = MongoClient("mongodb://localhost:27017")

# TODO: Get these three from kube envs - AUTH_LDAP_URL and AUTH_LDAP_USERDN. BASE_GROUP_DN is new and needs to be added
ldap_client = ldap.initialize('ldaps://ldap.ncsa.illinois.edu:636')
BASE_USER_DN = 'ou=people,dc=ncsa,dc=illinois,dc=edu'
BASE_GROUP_DN = 'cn=incore_user,ou=groups,dc=ncsa,dc=illinois,dc=edu'

incore_users = ldap_client.search_s(BASE_GROUP_DN, ldap.SCOPE_SUBTREE, '(cn=incore_user)')

for dn,entry in incore_users:
    members = entry['uniqueMember']
    for member in members:
        member = member.decode("utf-8", "ignore")
        member_id = re.search(r'uid=(.*?),ou', member).group(1)
        print(member_id)  # TODO: Remove print
        # Get all groups for each incore_user
        user_ldap_entry = ldap_client.search_s(BASE_USER_DN, ldap.SCOPE_SUBTREE, '(uid='+member_id+')')

        incore_groups = []
        for dn_user, entry_user in user_ldap_entry:
            user_groups = entry_user['memberOf']
            for group in user_groups:
                group = group.decode("utf-8", "ignore")
                group_name = re.search(r'cn=(.*?),ou', group).group(1)
                if "incore_" in group_name:  # is this a valid assumption to expect "incore_" in the name?
                    incore_groups.append(group_name)
            print(incore_groups) # TODO: Remove print

        # Get usergroups from mongo for this user
        mongo_user = mongo_client["spacedb"]["UserGroups"].find_one({"username": member_id})

        if mongo_user is None:
            # INSERT
            mongo_client["spacedb"]["UserGroups"].insert_one({
                "username": member_id,
                "className": "edu.illinois.ncsa.incore.common.models.UserGroups",
                "groups": incore_groups
            })
            print("inserted groups document for " + member_id)
        else:
            # compare groups and sync if needed
            if set(incore_groups) != set(mongo_user["groups"]):
                mongo_client["spacedb"]["UserGroups"].update_one(
                    {"username": member_id}, {"$set": {"groups": incore_groups}}
                )
                print("synced groups for " + member_id)
                print(incore_groups)
            else:
                print("No sync needed for " + member_id)


