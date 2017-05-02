#!/usr/bin/env python

import numpy as np
import matplotlib.pyplot as plt
import argparse

def autolabel(rects, ax):
    for rect in rects:
        height = rect.get_height()
        ax.text(rect.get_x() + rect.get_width()/2., 1.05*height, '%d' % int(height), ha='center', va='bottom')

def main(include_blur):
    N = 5 if include_blur else 4

    all_java_times_ms                = (311.2, 321.4, 325.0, 334.0, 15214.0)
    all_cpp_times_single_threaded_ms = (114.5, 123.0, 279.6, 188.0,  6566.6)
    all_cpp_times_multi_threaded_ms  = ( 45.6,  64.8, 118.2,  91.0,  2474.0)
    all_times = [all_java_times_ms, all_cpp_times_single_threaded_ms, all_cpp_times_multi_threaded_ms]

    no_blur_java_times_ms                = (311.2, 321.4, 325.0, 334.0)
    no_blur_cpp_times_single_threaded_ms = (114.5, 123.0, 279.6, 188.0)
    no_blur_cpp_times_multi_threaded_ms  = ( 45.6,  64.8, 118.2,  91.0)
    no_blur_times = [no_blur_java_times_ms, no_blur_cpp_times_single_threaded_ms, no_blur_cpp_times_multi_threaded_ms]

    times = all_times if include_blur else no_blur_times

    ind = np.arange(5 if include_blur else 4)
    width = 0.30

    fig, ax = plt.subplots()

    rects1 = ax.bar(ind,           times[0], width, color='r')
    rects2 = ax.bar(ind + width,   times[1], width, color='y')
    rects3 = ax.bar(ind + 2*width, times[2], width, color='g')

    ax.set_ylabel('Running Times (ms)')
    ax.set_ylim([0, 17000 if include_blur else 475])
    ax.set_title('Comparison of Java and C++ Running Times (With Blur)')
    ax.set_xticks(ind + width/2)
    ax.set_xticklabels(('Grayscale', 'Color Filter', 'Contrast', 'Brightness', 'Blur'))
    ax.legend((rects1[0], rects2[0], rects3[0]), ('Java', 'C++ (single-threaded)', 'C++ (multi-threaded)'))

    autolabel(rects1, ax)
    autolabel(rects2, ax)
    autolabel(rects3, ax)

    plt.show()

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--include-blur", help="Include blur data in plot", action="store_true")
    args = parser.parse_args()

    main(args.include_blur)
