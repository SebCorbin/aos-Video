// Copyright 2017 Archos SA
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.archos.mediacenter.video.browser.loader;

import android.content.Context;

import com.archos.mediacenter.video.R;

/**
 * Created by vapillon on 10/04/15.
 */
public class MoviesByYearLoader extends MoviesByLoader {

    private static final String DEFAULT_SORT = COLUMN_SUBSET_NAME+" COLLATE NOCASE DESC";

    public MoviesByYearLoader(Context context) {
        super(context);
        mSortOrder = DEFAULT_SORT;
        setSelection(getSelection(context));
    }

    public MoviesByYearLoader(Context context, String sortOrder) {
        super(context);
        mSortOrder = sortOrder;
        setSelection(getSelection(context));
    }



    public String getSelection(Context context) {
        return "SELECT\n" +
                "    _id,\n" +
                "    CASE\n" +
                "        WHEN m_year > 0 THEN m_year\n" +
                "        ELSE '"+context.getString(R.string.scrap_status_unknown)+"' \n" +
                "    END AS "+COLUMN_SUBSET_NAME+",\n" +
                "    group_concat( m_id ) AS "+COLUMN_LIST_OF_MOVIE_IDS+", -- movie id list\n" +
                "    group_concat( m_po_large_file ) AS "+COLUMN_LIST_OF_POSTER_FILES+", -- movie poster files list\n" +
                "    count( m_id ) AS "+COLUMN_NUMBER_OF_MOVIES+"   -- number of movie in list\n" +
                "FROM  ( \n" +
                "  SELECT _id, m_id, m_po_large_file, m_name, m_year FROM video\n"+
                "  WHERE m_id IS NOT NULL"+ getCommonSelection()+"\n"+
                "  ORDER BY m_name COLLATE NOCASE\n" +
                ") \n" +
                "GROUP BY name\n" +
                " ORDER BY "+mSortOrder;
    }
}
