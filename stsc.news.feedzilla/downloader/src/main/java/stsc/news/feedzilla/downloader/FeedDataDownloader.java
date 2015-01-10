package stsc.news.feedzilla.downloader;

import graef.feedzillajava.Article;
import graef.feedzillajava.Articles;
import graef.feedzillajava.Category;
import graef.feedzillajava.FeedZilla;
import graef.feedzillajava.Subcategory;

import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.joda.time.DateTime;

/**
 * {@link FeedDataDownloader} is a class that download newses from FeedZilla and
 * categories them.
 */
final class FeedDataDownloader {

	static {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	private void pause() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
	}

	FeedDataDownloader() {
		DateTime startOfDay = DateTime.now();
		startOfDay = startOfDay.minusDays(200);
		startOfDay = startOfDay.withTimeAtStartOfDay();
		final FeedZilla feed = new FeedZilla();

		int maxDsn = 0;

		final List<Category> categories = feed.getCategories();
		for (Category category : categories) {
			final Articles articles = feed.query().category(category).since(startOfDay).articles();
			for (Article a : articles.getArticles()) {
				if (a.getEnclosures() != null) {
					maxDsn = Math.max(maxDsn, a.getEnclosures().size());
				}
			}
			break;
		}
		System.out.println("-----------");
		System.out.println(maxDsn);

		// for (Category c : categories) {
		// System.out.println(c.getId());
		// System.out.println(c.getDisplayName());
		// System.out.println(c.getEnglishName());
		// System.out.println(c.getUrlName());
		// }
		// final List<Subcategory> subcategories = feed.getSubcategories(c);
		// System.out.println(" - " + subcategories.size());
		// pause();
		// for (Subcategory s : subcategories) {
		// try {
		// final Articles articles =
		// feed.query().category(c).subcategory(s).since(startOfDay).count(100).articles();
		// if (articles == null) {
		// System.out.println("--------- -------------");
		// } else {
		// System.out.println("Size: " + articles.getArticles().size());
		// // for (Article article : articles.getArticles()) {
		// // i++;
		// // map.put(article.getTitle(), article);
		// // pause();
		// // if (i % 1000 == 0) {
		// // System.out.println("articles " + i);
		// // }
		// // }
		// }
		// } catch (Exception e) {
		// System.err.println(i++ + " " + e.getMessage());
		// }
		// }
		// }
		// System.out.println("Size is: " + i + " by map: " + map.size());

	}

	public static void main(String[] args) {
		try {
			new FeedDataDownloader();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
