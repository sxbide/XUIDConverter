# XUIDConverter - Easy to Use XUID to Username and Username to XUID Converter

If you store a player's uniqueId and add Bedrock support, you may come across a uniqueId that looks like this:

```
00000000-0000-0000-0009-01fbca93ce9f
```

This is the uniqueId that Geyser converts. Inside this ID, you will find a hexadecimal XUID that needs to be converted to a decimal XUID. With the decimal XUID, you can then retrieve the original gamerTag from the Geyser API.

## Features
- Converts hexadecimal XUIDs to decimal XUIDs.
- Retrieves original gamerTags from the Geyser API.
- Implements caching to ensure no performance issues.

## Usage

1. **`getUsername(UUID)`**  
   This function takes a **UUID** (which includes the player's unique identifier) and converts it into a **decimal XUID**. Then, it uses the **Geyser API** to retrieve the player's original **username**.

2. **`getUniqueXUID(USERNAME)`**  
   This function takes a **username** and uses the **Geyser API** to retrieve the player's **XUID** (in hexadecimal format).

---
