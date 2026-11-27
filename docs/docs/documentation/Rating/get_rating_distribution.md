---
sidebar_position: 3
---

# Get Rating Distribution

Returns how many ratings of each value (1 to 5) a content item has. It's useful to draw the typical "stars" bar chart on a content page.

## Endpoint

- **URL:** `GET http://<your-domain>/api/rating/{id}/distribution`
  - `{id}` is the **content** ID.
- **Headers:**
  - `Authorization: Bearer <token>`

## Response Example

```json
{
  "1": 0,
  "2": 1,
  "3": 0,
  "4": 3,
  "5": 10
}
```

The keys are the rating values and the numbers are how many users gave that rating. In this example, 10 users rated it 5, 3 users rated it 4 and 1 user rated it 2.

The response always has the five keys, from `"1"` to `"5"`. Values with no ratings are `0`.

To get the average and the total number of ratings, use [Get Content Stats](../Stats/get_stats.md).

## Response Status

* `200 OK` – Returns the distribution. If the content has no ratings, or doesn't exist, all values are `0`
* `403 Forbidden` – Invalid or missing token
